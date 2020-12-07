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

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import base.SpecBase
import config.Constants.PDF
import connectors.NrsConnector
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.Play.materializer
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class PdfControllerSpec extends SpecBase {

  private val mockConnector: NrsConnector = mock[NrsConnector]

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .overrides(
        bind[NrsConnector].toInstance(mockConnector)
      )
  }

  private val controller: PdfController = injector.instanceOf[PdfController]

  private def getSourceString(result: Result): String = {
    val sink = Sink.fold[String, ByteString]("") { case (acc, str) =>
      acc + str.utf8String
    }
    Await.result(result.body.dataStream.runWith(sink), Duration.Inf)
  }

  ".getPdf" must {

    "return a successful response" when {
      "a pdf is generated" in {

        val responseBody: String = "abcdef"
        val contentLength: Long = 12345L

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(SuccessfulResponse(Source(List(ByteString(responseBody))), contentLength)))

        whenReady(controller.getPdf()(FakeRequest())) { result =>
          result.header.status mustBe OK

          result.header.headers mustEqual Map(
            CONTENT_TYPE -> PDF,
            CONTENT_LENGTH -> contentLength.toString,
            CONTENT_DISPOSITION -> "inline; filename.pdf"
          )

          getSourceString(result) mustEqual responseBody
        }
      }
    }

    "return an InternalServerError" when {

      "a BadRequestResponse is received from NRS" in {
        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(BadRequestResponse))

        val result: Future[Result] = controller.getPdf()(FakeRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "an UnauthorisedResponse is received from NRS" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(UnauthorisedResponse))

        val result: Future[Result] = controller.getPdf()(FakeRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "a NotFoundResponse is received from NRS" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(NotFoundResponse))

        val result: Future[Result] = controller.getPdf()(FakeRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "an InternalServerErrorResponse is received from NRS" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(InternalServerErrorResponse))

        val result: Future[Result] = controller.getPdf()(FakeRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
