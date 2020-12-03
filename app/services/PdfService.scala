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

package services

import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.Future

class PdfService {

  def pdf() =  {

      val lengthFromHeader = 20000L

      //      val outputStream = new java.io.ByteArrayOutputStream()
      //      val bufferedOutputStream = new java.io.BufferedOutputStream(outputStream, 1024).write(Array(1, 2, 3).map(_.toByte))

      //      bufferedOutputStream

      //      val source = Source.fromIterator(bufferedOutputStream)


      //      val source = new java.util.Array[Byte](lengthFromHeader)
      //      source.

//      val source = Source(List(ByteString.apply(Array("").map(_.toByte))))
//
//      Future.successful(Result(
//        header = play.api.mvc.ResponseHeader(
//          status = OK,
//          headers = Map(
//            "Content-Disposition" -> "inline; filename.pdf",
//            "Content-Type" -> "application/json",
//            "Content-Length" -> ""
//          )
//        ),
//        body = HttpEntity.Streamed(
//          data = source,
//          contentLength = Some(lengthFromHeader),
//          contentType = Some("application/pdf")
//        )
//      ))
  }
}
