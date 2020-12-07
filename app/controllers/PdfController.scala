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
import com.google.inject.Inject
import config.Constants.{CONTENT_LENGTH, CONTENT_TYPE}
import connectors.NrsConnector
import controllers.Assets.{BadGateway, OK}
import models.SuccessfulResponse
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Result}
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class PdfController @Inject()(action: DefaultActionBuilder, nrsConnector: NrsConnector) {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def pdf(): Action[AnyContent] = action.async {
    implicit request =>

      val payload = Json.toJson("")

      nrsConnector.getPdf(payload).map {
        case response@(_: SuccessfulResponse) =>

          Result(
            header = play.api.mvc.ResponseHeader(
              status = OK,
              headers = Map(
                "Content-Disposition" -> "inline; filename.pdf",
                CONTENT_TYPE -> "application/pdf",
                CONTENT_LENGTH -> response.length.toString
              )
            ),
            body = HttpEntity.Streamed(
              data = response.body,
              contentLength = Some(response.length),
              contentType = Some("application/pdf")
            )
          )
      }
  }

}
