/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import base.SpecBase
import config.Constants.PDF
import connectors.{NrsConnector, TrustDataConnector}
import helpers.JsonHelper.getJsonValueFromFile
import models._
import models.auditing.Events._
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import play.api.Play.materializer
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.NrsLockRepository
import services.AuditService
import utils.PdfFileNameGenerator

import java.time.LocalDateTime
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PdfControllerSpec extends SpecBase {

  private val mockTrustDataConnector: TrustDataConnector = mock[TrustDataConnector]
  private val mockPdfFileNameGenerator: PdfFileNameGenerator = mock[PdfFileNameGenerator]
  private val mockNrsConnector: NrsConnector = mock[NrsConnector]
  private val mockNrsLockRepository: NrsLockRepository = mock[NrsLockRepository]
  private val mockAuditService: AuditService = mock[AuditService]

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .overrides(
        bind[TrustDataConnector].toInstance(mockTrustDataConnector),
        bind[PdfFileNameGenerator].toInstance(mockPdfFileNameGenerator),
        bind[NrsConnector].toInstance(mockNrsConnector),
        bind[NrsLockRepository].toInstance(mockNrsLockRepository),
        bind[AuditService].toInstance(mockAuditService)
      )
  }

  private val identifier: String = "1234567890"

  private val fileName: String = "filename.pdf"

  private val trustJson: JsValue = getJsonValueFromFile("nrs-request-body.json")

  private val testDateTime: LocalDateTime = LocalDateTime.now()

  private val controller: PdfController = injector.instanceOf[PdfController]

  private def getSourceString(result: Result): String = {
    val sink = Sink.fold[String, ByteString]("") { case (acc, str) =>
      acc + str.utf8String
    }
    Await.result(result.body.dataStream.runWith(sink), Duration.Inf)
  }

  when(mockNrsLockRepository.setLock(any(), any())).thenReturn(Future.successful(true))

  "PdfController" when {
    ".getPdf" when {
      "there is no lock in mongo" must {
        "return a successful response" when {

          "a pdf is generated" in {

            val responseBody: String = "abcdef"
            val contentLength: Long = 12345L

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockPdfFileNameGenerator.generate(any())).thenReturn(Some(fileName))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(SuccessfulResponse(Source(List(ByteString(responseBody))), contentLength)))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe OK

              result.header.headers mustEqual Map(
                CONTENT_TYPE -> PDF,
                CONTENT_LENGTH -> contentLength.toString,
                CONTENT_DISPOSITION -> s"${appConfig.inlineOrAttachment}; filename=$fileName"
              )

              getSourceString(result) mustEqual responseBody

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(Some(trustJson)))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_DATA_RECEIVED), eqTo(null))(any(), any())
            }
          }
        }

        "return a ServiceUnavailable error" when {

          "IF is unavailable" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(ServiceUnavailableTrustDataResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(IF_ERROR), eqTo(Some(JsString("ServiceUnavailableTrustDataResponse"))))(any(), any())
            }
          }

          "NRS pings successfully but is unavailable when the getPdf call is made" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockPdfFileNameGenerator.generate(any())).thenReturn(Some(fileName))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(ServiceUnavailableResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(Some(trustJson)))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(Some(JsString("ServiceUnavailableResponse"))))(any(), any())
            }
          }

          "NRS ping fails" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(false))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(UNSUCCESSFUL_NRS_PING), eqTo(null))(any(), any())
            }
          }
        }

        "return an InternalServerError" when {

          "error retrieving trust data from IF" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(InternalServerErrorTrustDataResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(IF_ERROR), eqTo(Some(JsString("InternalServerErrorTrustDataResponse"))))(any(), any())
            }
          }

          "error retrieving PDF from NRS" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockPdfFileNameGenerator.generate(any())).thenReturn(Some(fileName))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(InternalServerErrorResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(Some(trustJson)))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(Some(JsString("InternalServerErrorResponse"))))(any(), any())
            }
          }
        }

        "return a BadRequest" when {

          "trust name not found in payload" in {

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockPdfFileNameGenerator.generate(any())).thenReturn(None)

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe BAD_REQUEST
            }
          }
        }
      }

      "there is a lock in mongo" must {
        "return a 429 (TOO_MANY_REQUESTS) response" in {

          reset(mockAuditService)

          when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

          when(mockNrsLockRepository.getLock(any()))
            .thenReturn(Future.successful(Some(NrsLock(locked = true, createdAt = testDateTime))))

          whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
            result.header.status mustBe TOO_MANY_REQUESTS

            verify(mockAuditService).audit(eqTo(TOO_MANY_PDF_GENERATION_REQUESTS), eqTo(null))(any(), any())
          }
        }
      }
    }
  }
}
