/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.util.ByteString
import base.SpecBase
import config.Constants.PDF
import connectors.{NrsConnector, TrustDataConnector}
import helpers.JsonHelper.getJsonValueFromFile
import models._
import models.auditing.Events._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, verify, when}
import play.api.Play.materializer
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.NrsLockRepository
import services.{AuditService, LocalDateTimeService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PdfControllerSpec extends SpecBase {

  private val mockTrustDataConnector: TrustDataConnector = mock(classOf[TrustDataConnector])
  private val mockNrsConnector: NrsConnector = mock(classOf[NrsConnector])
  private val mockNrsLockRepository: NrsLockRepository = mock(classOf[NrsLockRepository])
  private val mockAuditService: AuditService = mock(classOf[AuditService])
  private val mockLocalDateTimeService: LocalDateTimeService = mock(classOf[LocalDateTimeService])

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .overrides(
        bind[TrustDataConnector].toInstance(mockTrustDataConnector),
        bind[NrsConnector].toInstance(mockNrsConnector),
        bind[NrsLockRepository].toInstance(mockNrsLockRepository),
        bind[AuditService].toInstance(mockAuditService),
        bind[LocalDateTimeService].toInstance(mockLocalDateTimeService)
      )
  }

  private val identifier: String = "1234567890"

  private val trustJson: JsValue = getJsonValueFromFile("nrs-request-body.json")

  private val controller: PdfController = injector.instanceOf[PdfController]

  private def getSourceString(result: Result): String = {
    val sink = Sink.fold[String, ByteString]("") { case (acc, str) =>
      acc + str.utf8String
    }
    Await.result(result.body.dataStream.runWith(sink), Duration.Inf)
  }

  when(mockNrsLockRepository.setLock(any())).thenReturn(Future.successful(true))

  "PdfController" when {
    ".getPdf" when {
      "there is no lock in mongo" must {
        "return a successful response" when {

          "a pdf is generated" in {

            val responseBody: String = "abcdef"
            val contentLength: Long = 12345L
            val fileName = "1234567890-2021-02-03.pdf"

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(SuccessfulResponse(Source(List(ByteString(responseBody))), contentLength)))

            when(mockLocalDateTimeService.nowFormatted).thenReturn("2021-02-03")

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe OK

              result.header.headers mustEqual Map(
                CONTENT_DISPOSITION -> s"${appConfig.inlineOrAttachment}; filename=1234567890-2021-02-03.pdf"
              )

              result.body.contentLength.get mustBe contentLength
              result.body.contentType.get mustBe PDF

              getSourceString(result) mustEqual responseBody

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(trustJson))(any(), any())
              verify(mockAuditService).auditFileDetails(eqTo(NRS_DATA_RECEIVED), eqTo(FileDetails(fileName, PDF, contentLength)))(any(), any())
            }
          }
        }

        "return a ServiceUnavailable error" when {

          "IF is unavailable" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(ServiceUnavailableTrustDataResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(IF_ERROR), eqTo(JsString("ServiceUnavailableTrustDataResponse")))(any(), any())
            }
          }

          "NRS pings successfully but is unavailable when the getPdf call is made" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(ServiceUnavailableResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(trustJson))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(JsString("ServiceUnavailableResponse")))(any(), any())
            }
          }

          "NRS ping fails" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(false))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe SERVICE_UNAVAILABLE

              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(JsString("ServiceUnavailableResponse")))(any(), any())
            }
          }
        }

        "return an InternalServerError" when {

          "error retrieving trust data from IF" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(InternalServerErrorTrustDataResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(IF_ERROR), eqTo(JsString("InternalServerErrorTrustDataResponse")))(any(), any())
            }
          }

          "error retrieving PDF from NRS" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(InternalServerErrorResponse))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(IF_DATA_RECEIVED), eqTo(trustJson))(any(), any())
              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(JsString("InternalServerErrorResponse")))(any(), any())
            }
          }

          "bad request returned from NRS" in {

            reset(mockAuditService)

            when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

            when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

            when(mockTrustDataConnector.getTrustJson(any()))
              .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

            when(mockNrsConnector.getPdf(any())(any()))
              .thenReturn(Future.successful(BadRequestResponse()))

            whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
              result.header.status mustBe INTERNAL_SERVER_ERROR

              verify(mockAuditService).audit(eqTo(NRS_ERROR), eqTo(JsString("BadRequestResponse()")))(any(), any())
            }
          }
        }
      }

      "there is a lock in mongo" must {
        "return a 429 (TOO_MANY_REQUESTS) response" in {

          reset(mockAuditService)

          when(mockNrsConnector.ping()(any())).thenReturn(Future.successful(true))

          when(mockNrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(true))

          whenReady(controller.getPdf(identifier)(FakeRequest())) { result =>
            result.header.status mustBe TOO_MANY_REQUESTS

            verify(mockAuditService).audit(eqTo(EXCESSIVE_REQUESTS))(any(), any())
          }
        }
      }
    }
  }
}
