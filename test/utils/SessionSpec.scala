/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import base.SpecBase
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

class SessionSpec extends SpecBase {

  "Session" should {

    "return session ID when available" in {
      val sessionId = "test-session-id"
      implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

      val result = Session.id(hc)

      result mustBe sessionId
    }

    "return default message when session ID is not available" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val result = Session.id(hc)

      result mustBe "No Session ID available"
    }
  }
}
