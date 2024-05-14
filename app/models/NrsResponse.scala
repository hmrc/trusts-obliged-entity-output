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

package models

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.http.HeaderNames._
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.BodyReadable

trait NrsResponse

case class SuccessfulResponse(body: Source[ByteString, _], length: Long) extends NrsResponse
case class BadRequestResponse(parsedNRS400Response: String = "") extends NrsResponse
case object UnauthorisedResponse extends NrsResponse
case object NotFoundResponse extends NrsResponse
case object ServiceUnavailableResponse extends NrsResponse
case object InternalServerErrorResponse extends NrsResponse

object NrsResponse extends Logging {

  implicit val bodyReadable: BodyReadable[NrsResponse] = BodyReadable[NrsResponse] { response =>

    response.status match {
      case OK =>
        response.headers.get(CONTENT_LENGTH) match {
          case Some(Seq(length)) =>
            SuccessfulResponse(response.bodyAsSource, length.toLong)
          case _ =>
            logger.error(s"$CONTENT_LENGTH header is missing.")
            InternalServerErrorResponse
        }
      case BAD_REQUEST =>
        logger.debug(s"Payload does not conform to defined JSON schema - ${response.body}")
        logger.error(s"Payload does not conform to defined JSON schema")
        BadRequestResponse(parseNRS400ResponseBody(response.body))
      case UNAUTHORIZED =>
        logger.error("No X-API-Key provided or it is invalid.")
        UnauthorisedResponse
      case NOT_FOUND =>
        logger.error("Requested PDF template does not exist.")
        NotFoundResponse
      case SERVICE_UNAVAILABLE =>
        logger.debug(s"NRS service unavailable - ${response.body}.")
        logger.error(s"NRS service unavailable.")
        ServiceUnavailableResponse
      case _ =>
        logger.error("Internal server error response from NRS.")
        InternalServerErrorResponse
    }
  }

  def parseNRS400ResponseBody(body: String) = {
    /**
     * Matches anything inside square brackets UNLESS it is preceded by a caret symbol (^).
     */
    val regex = "(?<!\\^)\\[.*?\\]"

    Json.parse(body).as[JsArray].value.map { json =>
      json.\("message").getOrElse(Json.obj()).toString().replaceAll(regex, "[OBFUSCATED]")
    }.mkString("", ",\n", "")
  }

}
