/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.Inject
import config.AppConfig
import config.Constants.*
import connectors.NrsConnector
import controllers.actions.IdentifierActionProvider
import models.*
import models.auditing.Events.*
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.*
import repositories.NrsLockRepository
import services.{AuditService, TrustDataService, ValidationService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.PdfFileNameGenerator

import scala.concurrent.{ExecutionContext, Future}

class PdfController @Inject() (
  identifierAction: IdentifierActionProvider,
  nrsConnector: NrsConnector,
  trustDataService: TrustDataService,
  nrsLockRepository: NrsLockRepository,
  config: AppConfig,
  cc: ControllerComponents,
  pdfFileNameGenerator: PdfFileNameGenerator,
  auditService: AuditService,
  validationService: ValidationService
)(using ec: ExecutionContext)
    extends BackendController(cc) with Logging {

  def getPdf(identifier: String): Action[AnyContent] = identifierAction(identifier).async { request =>
    given IdentifierRequest[AnyContent] = request
    pingNrs(identifier)
  }

  private def logInfo(using request: IdentifierRequest[AnyContent]): String =
    s"[SessionId: ${request.sessionId}][${request.identifier}: ${request.identifier.value}]"

  private def pingNrs(identifier: String)(using request: IdentifierRequest[AnyContent]): Future[Result] =
    nrsConnector.ping().flatMap {
      case true =>
        logger.info(s"$logInfo Successfully pinged NRS.")
        getLockStatus(identifier)
      case _    =>
        auditService.audit(NRS_ERROR, JsString(s"$ServiceUnavailableResponse"))
        logger.error(s"$logInfo Failed to ping NRS. Aborted PDF request.")
        Future.successful(ServiceUnavailable)
    }

  private def getLockStatus(identifier: String)(using request: IdentifierRequest[AnyContent]): Future[Result] =
    nrsLockRepository.getLock(request.internalId, identifier).flatMap {
      case true =>
        auditService.audit(EXCESSIVE_REQUESTS)
        Future.successful(TooManyRequests)
      case _    =>
        setLockStatus(identifier, lock = true).flatMap { _ =>
          getTrustJson(identifier)
        }
    }

  private def setLockStatus(identifier: String, lock: Boolean)(using
    request: IdentifierRequest[AnyContent]
  ): Future[Boolean] =
    nrsLockRepository.setLock(NrsLock.build(request.internalId, identifier, lock))

  private def dataReceivedEvent: String =
    if (config.useHipObligedEntities) HIP_DATA_RECEIVED else IF_DATA_RECEIVED

  private def dataErrorEvent: String =
    if (config.useHipObligedEntities) HIP_ERROR else IF_ERROR

  private def downstreamPlatformName: String =
    if (config.useHipObligedEntities) HIP_NAME else IFS_NAME

  private def getTrustJson(identifier: String)(using request: IdentifierRequest[AnyContent]): Future[Result] =
    trustDataService.getTrustJson(request.identifier).flatMap {
      case SuccessfulIfsTrustDataResponse(payload) =>
        auditService.audit(dataReceivedEvent, payload)
        validationService.get(config.trustsIfsObligedEntityDataSchema).validate(payload.toString()) match {
          case Right(_)               =>
            processValidatedPayload(identifier, payload)
          case Left(validationErrors) =>
            logger.warn(
              s"[PdfController][getTrustJson][Session ID: ${request.sessionId}] problem with payload: $validationErrors"
            )
            Future.successful(InternalServerError)
        }
      case SuccessfulHipTrustDataResponse(payload) =>
        auditService.audit(dataReceivedEvent, payload)
        processValidatedPayload(identifier, payload)
      case e                                       =>
        auditService.audit(dataErrorEvent, JsString(s"$e"))
        e match {
          case ServiceUnavailableTrustDataResponse =>
            logger.error(s"$logInfo ServiceUnavailable returned from $downstreamPlatformName.")
            Future.successful(ServiceUnavailable)
          case _                                   =>
            logger.error(s"$logInfo Error retrieving trust data from $downstreamPlatformName")
            Future.successful(InternalServerError)
        }
    }

  private def processValidatedPayload(identifier: String, payload: JsValue)(using
    request: IdentifierRequest[AnyContent]
  ): Future[Result] = {
    val fileName = pdfFileNameGenerator.generate(identifier)
    generatePdf(identifier, payload, fileName)
  }

  private def generatePdf(identifier: String, payload: JsValue, fileName: String)(using
    request: IdentifierRequest[AnyContent]
  ): Future[Result] =
    nrsConnector.getPdf(payload).flatMap {
      case response: SuccessfulResponse =>
        auditService.auditFileDetails(NRS_DATA_RECEIVED, FileDetails(fileName, PDF, response.length))
        setLockStatus(identifier, lock = false).map { _ =>
          pdf(fileName, response)
        }
      case e                            =>
        auditService.audit(NRS_ERROR, JsString(s"$e"))
        e match {
          case BadRequestResponse(body)   =>
            if (config.logNRS400ResponseBody) {
              logger.error(s"Response from NRS - $body")
            }
            Future.successful(InternalServerError)
          case ServiceUnavailableResponse =>
            logger.error(s"$logInfo ServiceUnavailable returned from NRS.")
            Future.successful(ServiceUnavailable)
          case _                          =>
            logger.error(s"$logInfo Error retrieving PDF from NRS")
            Future.successful(InternalServerError)
        }
    }

  private def pdf(fileName: String, response: SuccessfulResponse): Result =
    Result(
      header = ResponseHeader(
        status = OK,
        headers = Map(
          CONTENT_DISPOSITION -> s"${config.inlineOrAttachment}; filename=$fileName"
        )
      ),
      body = HttpEntity.Streamed(
        data = response.body,
        contentLength = Some(response.length),
        contentType = Some(PDF)
      )
    )

}
