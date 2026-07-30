/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.Try

trait TrustDataResponse

case class SuccessfulIfsTrustDataResponse(body: JsValue) extends TrustDataResponse
case class SuccessfulHipTrustDataResponse(body: JsValue) extends TrustDataResponse
case object BadRequestTrustDataResponse extends TrustDataResponse
case object UnprocessableEntityTrustDataResponse extends TrustDataResponse
case object ServiceUnavailableTrustDataResponse extends TrustDataResponse
case object UnauthorisedTrustDataResponse extends TrustDataResponse
case object ForbiddenTrustDataResponse extends TrustDataResponse
case object NotFoundTrustDataResponse extends TrustDataResponse
case object InternalServerErrorTrustDataResponse extends TrustDataResponse

object TrustDataResponse extends Logging {

  def ifsHttpReads(identifier: Identifier): HttpReads[TrustDataResponse] =
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK                   =>
          SuccessfulIfsTrustDataResponse(Json.parse(response.body))
        case BAD_REQUEST          =>
          logger.error(s"[UTR/URN: ${identifier.value}] Invalid identifier - ${response.body}.")
          BadRequestTrustDataResponse
        case UNPROCESSABLE_ENTITY =>
          logger.error(s"[UTR/URN: ${identifier.value}] Could not be processed - ${response.body}.")
          UnprocessableEntityTrustDataResponse
        case SERVICE_UNAVAILABLE  =>
          logger.error(s"[UTR/URN: ${identifier.value}] IF service unavailable - ${response.body}.")
          ServiceUnavailableTrustDataResponse
        case UNAUTHORIZED         =>
          logger.error(
            s"[UTR/URN: ${identifier.value}] No Authorization header (bearer token) provided or it is invalid."
          )
          UnauthorisedTrustDataResponse
        case FORBIDDEN            =>
          logger.error(s"[UTR/URN: ${identifier.value}] No Environment header provided or it is invalid.")
          ForbiddenTrustDataResponse
        case NOT_FOUND            =>
          logger.error(s"[UTR/URN: ${identifier.value}] Resource not found for the provided identifier.")
          NotFoundTrustDataResponse
        case _                    =>
          logger.error(s"[UTR/URN: ${identifier.value}] Internal server error response from IF.")
          InternalServerErrorTrustDataResponse
      }

  def hipHttpReads(identifier: Identifier): HttpReads[TrustDataResponse] =
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK                   =>
          SuccessfulHipTrustDataResponse(Json.parse(response.body))
        case BAD_REQUEST          =>
          logger.error(s"[UTR/URN: ${identifier.value}] Invalid identifier - ${response.body}.")
          BadRequestTrustDataResponse
        case UNAUTHORIZED         =>
          logger.error(
            s"[UTR/URN: ${identifier.value}] No Authorization header (bearer token) provided or it is invalid."
          )
          UnauthorisedTrustDataResponse
        case FORBIDDEN            =>
          logger.error(s"[UTR/URN: ${identifier.value}] No Environment header provided or it is invalid.")
          ForbiddenTrustDataResponse
        case NOT_FOUND            =>
          logger.error(s"[UTR/URN: ${identifier.value}] Resource not found for the provided identifier.")
          NotFoundTrustDataResponse
        case UNPROCESSABLE_ENTITY =>
          mapHipUnprocessableEntity(identifier, response)
        case _                    =>
          logger.error(s"[UTR/URN: ${identifier.value}] Internal server error response from HIP - ${response.body}.")
          InternalServerErrorTrustDataResponse
      }

  private def mapHipUnprocessableEntity(identifier: Identifier, response: HttpResponse): TrustDataResponse =
    Try(response.json.as[HipCustomErrResponse].error.errorId).fold(
      _ => {
        logger.error(s"[UTR/URN: ${identifier.value}] Unprocessable HIP response - ${response.body}.")
        InternalServerErrorTrustDataResponse
      },
      {
        case "000" =>
          logger.error(s"[UTR/URN: ${identifier.value}] Resource not found for the provided identifier.")
          NotFoundTrustDataResponse
        case "003" =>
          logger.error(s"[UTR/URN: ${identifier.value}] Could not be processed - ${response.body}.")
          UnprocessableEntityTrustDataResponse
        case code  =>
          logger.error(s"[UTR/URN: ${identifier.value}] HIP errorId $code - ${response.body}.")
          UnprocessableEntityTrustDataResponse
      }
    )

}
