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

import config.Constants._
import play.api.Logging
import play.api.http.Status._
import play.api.libs.ws.BodyReadable
import play.api.libs.ws.ahc.StandaloneAhcWSResponse
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

trait NrsResponse

case class SuccessfulResponse(body: Array[Byte], length: Long) extends NrsResponse
case object BadRequestResponse extends NrsResponse
case object UnauthorisedResponse extends NrsResponse
case object NotFoundResponse extends NrsResponse
case object InternalServerErrorResponse extends NrsResponse

object NrsResponse extends Logging {

  implicit val httpReads: HttpReads[NrsResponse] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK =>
        response.header(CONTENT_LENGTH) match {
          case Some(length) => SuccessfulResponse(response.body.getBytes, length.toLong)
          case _ =>
            logger.warn(s"$CONTENT_LENGTH header missing from response.")
            ???
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
      case _ =>
        logger.error("Service unavailable response from NRS.")
        InternalServerErrorResponse
    }
  }

  implicit val bodyReadable: BodyReadable[NrsResponse] = BodyReadable[NrsResponse] { response =>
    import play.shaded.ahc.org.asynchttpclient.{Response => AHCResponse}
    val ahcResponse: AHCResponse = response.asInstanceOf[StandaloneAhcWSResponse].underlying[AHCResponse]

    ahcResponse.getStatusCode match {
      case OK =>
        SuccessfulResponse(ahcResponse.getResponseBody.getBytes, ahcResponse.getHeaders.get(CONTENT_LENGTH).toLong)
      case BAD_REQUEST =>
        logger.error(s"Payload does not conform to defined JSON schema - ${ahcResponse.getResponseBody}")
        BadRequestResponse
      case UNAUTHORIZED =>
        logger.error("No X-API-Key provided or it is invalid.")
        UnauthorisedResponse
      case NOT_FOUND =>
        logger.error("Requested PDF template does not exist.")
        NotFoundResponse
      case _ =>
        logger.error("Service unavailable response from NRS.")
        InternalServerErrorResponse
    }
  }

}
