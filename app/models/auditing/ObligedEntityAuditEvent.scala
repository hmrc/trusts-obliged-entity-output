/*
 * Copyright 2021 HM Revenue & Customs
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

package models.auditing

import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup

case class ObligedEntityAuditEvent(internalAuthId: String,
                                   identifier: String,
                                   affinity: AffinityGroup,
                                   sessionId: String,
                                   dateTime: String,
                                   response: Option[JsValue])

object ObligedEntityAuditEvent {
  implicit val formats: Format[ObligedEntityAuditEvent] = Json.format[ObligedEntityAuditEvent]
}
