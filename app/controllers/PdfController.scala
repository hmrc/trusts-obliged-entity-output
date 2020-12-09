/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import com.google.inject.Inject
import config.AppConfig
import config.Constants._
import connectors.{NrsConnector, TrustDataConnector}
import controllers.actions.IdentifierActionProvider
import models.{NrsLock, SuccessfulResponse, SuccessfulTrustDataResponse}
import play.api.Logging
import play.api.http.HttpEntity
import play.api.mvc._
import repositories.NrsLockRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.PdfFileNameGenerator

import scala.concurrent.{ExecutionContext, Future}

class PdfController @Inject()(identifierAction: IdentifierActionProvider,
                              nrsConnector: NrsConnector,
                              trustDataConnector: TrustDataConnector,
                              nrsLockRepository: NrsLockRepository,
                              config: AppConfig,
                              cc: ControllerComponents,
                              pdfFileNameGenerator: PdfFileNameGenerator) extends BackendController(cc) with Logging {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def getPdf(identifier: String): Action[AnyContent] = identifierAction(identifier).async {
    implicit request =>

      lazy val logInfo: String = s"[SessionId: ${request.sessionId}][${request.identifier}: ${request.identifier.value}]"

      nrsLockRepository.getLock(identifier).flatMap {
        case Some(NrsLock(true, _)) => Future.successful(TooManyRequests)
        case _ =>
          nrsLockRepository.setLock(identifier, lock = NrsLock(locked = true, createdAt = LocalDateTime.now())).flatMap { _ =>
            trustDataConnector.getTrustJson(request.identifier) flatMap {
              case SuccessfulTrustDataResponse(payload) =>
                pdfFileNameGenerator.generate(payload) match {
                  case Some(fileName) =>
                    nrsConnector.getPdf(payload).flatMap {
                      case response: SuccessfulResponse =>
                        nrsLockRepository.setLock(identifier, lock = NrsLock(locked = false, createdAt = LocalDateTime.now())).map { _ =>
                          Result(
                            header = ResponseHeader(
                              status = OK,
                              headers = Map(
                                CONTENT_DISPOSITION -> s"${config.inlineOrAttachment}; filename=$fileName",
                                CONTENT_TYPE -> PDF,
                                CONTENT_LENGTH -> response.length.toString
                              )
                            ),
                            body = HttpEntity.Streamed(
                              data = response.body,
                              contentLength = Some(response.length),
                              contentType = Some(PDF)
                            )
                          )
                        }
                      case e =>
                        logger.error(s"$logInfo Error retrieving PDF from NRS: $e.")
                        Future.successful(InternalServerError)
                    }
                  case _ =>
                    logger.error(s"$logInfo Trust name not found in payload.")
                    Future.successful(BadRequest)
                }
              case e =>
                logger.error(s"$logInfo Error retrieving trust data from IF: $e.")
                Future.successful(InternalServerError)
            }
          }
      }
  }
}
