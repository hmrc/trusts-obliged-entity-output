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

import com.google.inject.Inject
import config.AppConfig
import config.Constants._
import connectors.NrsConnector
import controllers.Assets._
import models.SuccessfulResponse
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc._
import utils.PdfFileNameGenerator

import scala.concurrent.ExecutionContext

class PdfController @Inject()(action: DefaultActionBuilder,
                              nrsConnector: NrsConnector,
                              config: AppConfig) extends Logging {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def getPdf(): Action[AnyContent] = action.async {
    implicit request =>

      val payload = Json.toJson("") // TODO - get payload from request.body
      val fileName: String = PdfFileNameGenerator.generate(payload)

      nrsConnector.getPdf(payload).map {
        case response@(_: SuccessfulResponse) =>

          Result(
            header = ResponseHeader(
              status = OK,
              headers = Map(
                CONTENT_DISPOSITION -> s"${config.inlineOrAttachment}; filename=$fileName.pdf",
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
        case e =>
          logger.error(s"Error retrieving PDF from NRS: $e")
          InternalServerError
      }
  }

}
