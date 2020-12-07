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

  def pdf()(implicit ec: ExecutionContext): Action[AnyContent] = action.async {
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
                CONTENT_LENGTH -> response.contentLength.map(_.head).getOrElse("")
              )
            ),
            body = HttpEntity.Streamed(
              data = Source(List(ByteString.apply(response.body))),
              contentLength = response.contentLength.map(_.head.toLong),
              contentType = Some("application/pdf")
            )
          )
      }
  }

//  def pdfStreamed()(implicit ec: ExecutionContext): Action[AnyContent] = action.async {
//    implicit request =>
//    // Make the request
//    nrsConnector.getPdf(Json.toJson("")).stream().map {
//      case response@(_: SuccessfulResponse) =>
//        // Get the content type
//        val contentType = response.headers
//          .get("Content-Type")
//          .flatMap(_.headOption)
//          .getOrElse("application/pdf")
//
//        // If there's a content length, send that, otherwise return the body chunked
//        response.headers.get("Content-Length") match {
//          case Some(Seq(length)) =>
//            Ok.sendEntity(HttpEntity.Streamed(response.bodyAsSource, Some(length.toLong), Some(contentType)))
//          case _ =>
//            Ok.chunked(response.bodyAsSource).as(contentType)
//        }
//      case _ =>
//        BadGateway
//    }
//  }
}
