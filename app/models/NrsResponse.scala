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

import config.Constants.{CONTENT_LENGTH, CONTENT_TYPE}
import play.api.Logging
import play.api.http.Status._
import play.api.mvc.Headers
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

trait NonRepudiationServiceResponse

case class SuccessfulResponse(contentLength: Option[Seq[String]], body: Array[Byte]) extends NonRepudiationServiceResponse
case object BadRequestResponse extends NonRepudiationServiceResponse
case object UnauthorisedResponse extends NonRepudiationServiceResponse
case object NotFoundResponse extends NonRepudiationServiceResponse
case object InternalServerErrorResponse extends NonRepudiationServiceResponse

object NonRepudiationServiceResponse extends Logging {

  implicit lazy val httpReads: HttpReads[NonRepudiationServiceResponse] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK =>
        SuccessfulResponse(response.headers.get(CONTENT_LENGTH), response.body.getBytes)
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

}
