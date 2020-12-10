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

package models

import akka.stream.scaladsl.Source
import akka.util.ByteString
import controllers.Assets.CONTENT_LENGTH
import play.api.Logging
import play.api.http.Status._
import play.api.libs.ws.BodyReadable

trait NrsResponse

case class SuccessfulResponse(body: Source[ByteString, _], length: Long) extends NrsResponse
case object BadRequestResponse extends NrsResponse
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
        logger.error(s"Payload does not conform to defined JSON schema - ${response.body}")
        BadRequestResponse
      case UNAUTHORIZED =>
        logger.error("No X-API-Key provided or it is invalid.")
        UnauthorisedResponse
      case NOT_FOUND =>
        logger.error("Requested PDF template does not exist.")
        NotFoundResponse
      case SERVICE_UNAVAILABLE =>
        logger.error(s"Service Unavailable - ${response.body}.")
        ServiceUnavailableResponse
      case _ =>
        logger.error("Service unavailable response from NRS.")
        InternalServerErrorResponse
    }
  }

}
