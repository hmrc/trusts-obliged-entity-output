/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase

class IdentifierSpec extends SpecBase {

  "Identifier" when {

    "UTR" when {
      ".toString" must {
        "return UTR" in {
          val identifier: Identifier = UTR("1234567890")
          identifier.toString mustEqual "UTR"
          s"$identifier" mustEqual "UTR"
        }
      }
    }

    "URN" when {
      ".toString" must {
        "return URN" in {

          val identifier: Identifier = URN("NTTRUST12345678")
          identifier.toString mustEqual "URN"
          s"$identifier" mustEqual "URN"
        }
      }
    }
  }
}
