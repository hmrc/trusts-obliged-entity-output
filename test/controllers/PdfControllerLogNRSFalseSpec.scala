/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import connectors.{NrsConnector, TrustDataConnector}
import helpers.JsonHelper.getJsonValueFromFile
import models._
import models.auditing.Events._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, JsValue}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.NrsLockRepository
import services._

import scala.concurrent.Future

class PdfControllerLogNRSFalseSpec extends SpecBase {

  private val mockTrustDataConnector: TrustDataConnector = mock(classOf[TrustDataConnector])
  private val mockNrsConnector: NrsConnector = mock(classOf[NrsConnector])
  private val mockNrsLockRepository: NrsLockRepository = mock(classOf[NrsLockRepository])
  private val mockAuditService: AuditService = mock(classOf[AuditService])
  private val mockLocalDateTimeService: LocalDateTimeService = mock(classOf[LocalDateTimeService])
  private val mockValidationService: ValidationService = mock(classOf[ValidationService])
  private val defaultValidator: Validator = mock(classOf[Validator])

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "metrics.enabled" -> false,
          "auditing.enabled" -> false,
          "features.logNRS400ResponseBody" -> false
        ): _*
      )
      .overrides(
        bind[TrustDataConnector].toInstance(mockTrustDataConnector),
        bind[NrsConnector].toInstance(mockNrsConnector),
        bind[NrsLockRepository].toInstance(mockNrsLockRepository),
        bind[AuditService].toInstance(mockAuditService),
        bind[LocalDateTimeService].toInstance(mockLocalDateTimeService),
        bind[ValidationService].toInstance(mockValidationService)
      )
  }

  private val identifier: String = "1234567890"

  private val trustJson: JsValue = getJsonValueFromFile("nrs-request-body.json")

  private val controller: PdfController = injector.instanceOf[PdfController]

  "PdfController" when {
    ".getPdf" when {
      "there is no lock in mongo" must {
        "return an InternalServerError" when {

          "bad request returned from NRS with logging disabled" in {

            reset(mockAuditService)

            when(mockNrsLockRepository.setLock(any())).thenReturn(Future.successful(true))
            when(mockValidationService.get(any())).thenReturn(defaultValidator)
            when(defaultValidator.validate(any[String]())).thenReturn(Right(()))
            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))
            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))
            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))
            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(BadRequestResponse("Invalid request body")))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(trustJson))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(JsString("BadRequestResponse(Invalid request body)")))(any(), any())
            }
          }

        }
      }
    }
  }
}
