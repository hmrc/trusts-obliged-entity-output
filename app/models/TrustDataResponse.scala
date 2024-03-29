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

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

trait TrustDataResponse

case class SuccessfulTrustDataResponse(body: JsValue) extends TrustDataResponse
case object BadRequestTrustDataResponse extends TrustDataResponse
case object UnprocessableEntityTrustDataResponse extends TrustDataResponse
case object ServiceUnavailableTrustDataResponse extends TrustDataResponse
case object UnauthorisedTrustDataResponse extends TrustDataResponse
case object ForbiddenTrustDataResponse extends TrustDataResponse
case object NotFoundTrustDataResponse extends TrustDataResponse
case object InternalServerErrorTrustDataResponse extends TrustDataResponse

object TrustDataResponse extends Logging {

  implicit def httpReads(identifier: Identifier): HttpReads[TrustDataResponse] = (_: String, _: String, response: HttpResponse) => {

    response.status match {
      case OK =>
        SuccessfulTrustDataResponse(Json.parse(response.body))
      case BAD_REQUEST =>
        logger.error(s"[UTR/URN: ${identifier.value}] Invalid identifier - ${response.body}.")
        BadRequestTrustDataResponse
      case UNPROCESSABLE_ENTITY =>
        logger.error(s"[UTR/URN: ${identifier.value}] Could not be processed - ${response.body}.")
        UnprocessableEntityTrustDataResponse
      case SERVICE_UNAVAILABLE =>
        logger.error(s"[UTR/URN: ${identifier.value}] IF service unavailable - ${response.body}.")
        ServiceUnavailableTrustDataResponse
      case UNAUTHORIZED =>
        logger.error(s"[UTR/URN: ${identifier.value}] No Authorization header (bearer token) provided or it is invalid.")
        UnauthorisedTrustDataResponse
      case FORBIDDEN =>
        logger.error(s"[UTR/URN: ${identifier.value}] No Environment header provided or it is invalid.")
        ForbiddenTrustDataResponse
      case NOT_FOUND =>
        logger.error(s"[UTR/URN: ${identifier.value}] Resource not found for the provided identifier.")
        NotFoundTrustDataResponse
      case _ =>
        logger.error(s"[UTR/URN: ${identifier.value}] Internal server error response from IF.")
        InternalServerErrorTrustDataResponse
    }
  }

}
