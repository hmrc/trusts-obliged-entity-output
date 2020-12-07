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


import akka.stream.scaladsl.Source
import akka.util.ByteString
import base.SpecBase
import play.api.test.Helpers._
import connectors.NrsConnector
import models.{BadRequestResponse, InternalServerErrorResponse, NotFoundResponse, SuccessfulResponse, UnauthorisedResponse}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

import scala.concurrent.Future

class PdfControllerSpec extends SpecBase {

  val connector = mock[NrsConnector]

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .overrides(
        bind[NrsConnector].toInstance(connector)
      )
  }

  "getPdf" must {

    "return a successful response when a pdf is generated" in {

      when(connector.getPdf(any())(any()))
        .thenReturn(Future.successful(SuccessfulResponse(Source(List(ByteString.apply("abcdef"))), 12345L)))

      val controller = injector.instanceOf[PdfController]

      whenReady(controller.getPdf()(FakeRequest())) { result =>
        result.header.status mustBe OK
        result.header.headers mustEqual Map(
          CONTENT_TYPE -> "application/pdf",
          CONTENT_LENGTH -> "12345",
          CONTENT_DISPOSITION -> "inline; filename.pdf"
        )
      }
    }

    "return an InternalServerError when a BadRequestResponse is returned from NRS" in {

      when(connector.getPdf(any())(any()))
        .thenReturn(Future.successful(BadRequestResponse))

      val controller = injector.instanceOf[PdfController]

      val result = controller.getPdf()(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an InternalServerError when an UnauthorisedResponse is returned from NRS" in {

      when(connector.getPdf(any())(any()))
        .thenReturn(Future.successful(UnauthorisedResponse))

      val controller = injector.instanceOf[PdfController]

      val result = controller.getPdf()(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an InternalServerError when a NotFoundResponse is returned from NRS" in {

      when(connector.getPdf(any())(any()))
        .thenReturn(Future.successful(NotFoundResponse))

      val controller = injector.instanceOf[PdfController]

      val result = controller.getPdf()(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an InternalServerError when an InternalServerErrorResponse is returned from NRS" in {

      when(connector.getPdf(any())(any()))
        .thenReturn(Future.successful(InternalServerErrorResponse))

      val controller = injector.instanceOf[PdfController]

      val result = controller.getPdf()(FakeRequest())

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
