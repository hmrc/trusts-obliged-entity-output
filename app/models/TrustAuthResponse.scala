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

import play.api.libs.json.{Format, Json, Reads, __}

sealed trait TrustAuthResponse

object TrustAuthResponse {

  given reads: Reads[TrustAuthResponse] =
    __.read[TrustAuthAllowed].widen[TrustAuthResponse] orElse
      __.read[TrustAuthAgentAllowed].widen[TrustAuthResponse] orElse
      __.read[TrustAuthDenied].widen[TrustAuthResponse]

}

case class TrustAuthAllowed(authorised: Boolean = true) extends TrustAuthResponse

case object TrustAuthAllowed {

  given format: Format[TrustAuthAllowed] = Format(
    (__ \ "authorised").read[Boolean].map(TrustAuthAllowed.apply),
    Json.writes[TrustAuthAllowed]
  )

}

case class TrustAuthAgentAllowed(arn: String) extends TrustAuthResponse

case object TrustAuthAgentAllowed {
  given format: Format[TrustAuthAgentAllowed] = Json.format[TrustAuthAgentAllowed]
}

case class TrustAuthDenied(redirectUrl: String) extends TrustAuthResponse

case object TrustAuthDenied {
  given format: Format[TrustAuthDenied] = Json.format[TrustAuthDenied]
}

case object TrustAuthInternalServerError extends TrustAuthResponse
