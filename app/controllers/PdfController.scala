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
import controllers.Assets.OK
import play.api.http.HttpEntity
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Result}

import scala.concurrent.Future

class PdfController @Inject()(action: DefaultActionBuilder) {

  def pdf(): Action[AnyContent] = action.async {
    implicit request =>

      val lengthFromHeader = 20000L
      val byteString = ByteString.apply(Array("").map(_.toByte))
      val source = Source(List(byteString))

      Future.successful(Result(
        header = play.api.mvc.ResponseHeader(
          status = OK,
          headers = Map(
            "Content-Disposition" -> "inline; filename.pdf",
            "Content-Type" -> "application/json",
            "Content-Length" -> ""
          )
        ),
        body = HttpEntity.Streamed(
          data = source,
          contentLength = Some(lengthFromHeader),
          contentType = Some("application/pdf")
        )
      ))
  }
}
