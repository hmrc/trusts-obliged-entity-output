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

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import base.SpecBase
import config.Constants.PDF
import connectors.{NrsConnector, TrustDataConnector}
import controllers.actions.{FakeIdentifierActionProvider, IdentifierActionProvider}
import helpers.JsonHelper.getJsonValueFromFile
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.Play.materializer
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import repositories.NrsLockRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import utils.PdfFileNameGenerator

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PdfControllerSpec extends SpecBase {

  private val mockTrustDataConnector: TrustDataConnector = mock[TrustDataConnector]
  private val mockPdfFileNameGenerator: PdfFileNameGenerator = mock[PdfFileNameGenerator]
  private val mockNrsConnector: NrsConnector = mock[NrsConnector]
  private val nrsLockRepository: NrsLockRepository = mock[NrsLockRepository]

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .overrides(
        bind[TrustDataConnector].toInstance(mockTrustDataConnector),
        bind[PdfFileNameGenerator].toInstance(mockPdfFileNameGenerator),
        bind[NrsConnector].toInstance(mockNrsConnector),
        bind[NrsLockRepository].toInstance(nrsLockRepository),
        bind[IdentifierActionProvider].toInstance(new FakeIdentifierActionProvider(Helpers.stubControllerComponents().parsers.default, Organisation))
      )
  }

  private val utr: String = "1234567890"

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

  when(nrsLockRepository.setLock(any(), any())).thenReturn(Future.successful(true))

  ".getPdf" when {
    "there is no lock in mongo" must {
      "return a successful response" when {

        "a pdf is generated" in {

          val responseBody: String = "abcdef"
          val contentLength: Long = 12345L

          when(nrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

          when(mockTrustDataConnector.getTrustJson(any())(any(), any()))
            .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

          when(mockPdfFileNameGenerator.generate(any())).thenReturn(Some(fileName))

          when(mockNrsConnector.getPdf(any())(any()))
            .thenReturn(Future.successful(SuccessfulResponse(Source(List(ByteString(responseBody))), contentLength)))

          whenReady(controller.getPdf(utr)(FakeRequest())) { result =>
            result.header.status mustBe OK

            result.header.headers mustEqual Map(
              CONTENT_TYPE -> PDF,
              CONTENT_LENGTH -> contentLength.toString,
              CONTENT_DISPOSITION -> s"${appConfig.inlineOrAttachment}; filename=$fileName"
            )

            getSourceString(result) mustEqual responseBody
          }
        }
      }

      "return an InternalServerError" when {

        "error retrieving trust data from IF" in {

          when(nrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

          when(mockTrustDataConnector.getTrustJson(any())(any(), any()))
            .thenReturn(Future.successful(InternalServerErrorTrustDataResponse))

          whenReady(controller.getPdf(utr)(FakeRequest())) { result =>
            result.header.status mustBe INTERNAL_SERVER_ERROR
          }
        }

        "error retrieving PDF from NRS" in {

          when(nrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

          when(mockTrustDataConnector.getTrustJson(any())(any(), any()))
            .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

          when(mockPdfFileNameGenerator.generate(any())).thenReturn(Some(fileName))

          when(mockNrsConnector.getPdf(any())(any()))
            .thenReturn(Future.successful(InternalServerErrorResponse))

          whenReady(controller.getPdf(utr)(FakeRequest())) { result =>
            result.header.status mustBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "return a BadRequest" when {

        "trust name not found in payload" in {

          when(nrsLockRepository.getLock(any())).thenReturn(Future.successful(None))

          when(mockTrustDataConnector.getTrustJson(any())(any(), any()))
            .thenReturn(Future.successful(SuccessfulTrustDataResponse(trustJson)))

          when(mockPdfFileNameGenerator.generate(any())).thenReturn(None)

          whenReady(controller.getPdf(utr)(FakeRequest())) { result =>
            result.header.status mustBe BAD_REQUEST
          }
        }
      }
    }

    "there is a lock in mongo" must {
      "return a 429 (TOO_MANY_REQUESTS) response" in {
        when(nrsLockRepository.getLock(any()))
          .thenReturn(Future.successful(Some(NrsLock(locked = true, createdAt = testDateTime))))

        whenReady(controller.getPdf(utr)(FakeRequest())) { result =>
          result.header.status mustBe TOO_MANY_REQUESTS
        }
      }
    }
  }
}
