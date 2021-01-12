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

package models

import org.scalacheck.Arbitrary.arbitrary
import base.SpecBase
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class IdentifierSpec extends SpecBase with ScalaCheckPropertyChecks {

  "Identifier" when {

    "UTR" when {
      ".toString" must {
        "return UTR" in {
          forAll(arbitrary[String]) {
            str =>
              val identifier: Identifier = UTR(str)
              identifier.toString mustEqual "UTR"
              s"$identifier" mustEqual "UTR"
          }
        }
      }
    }

    "URN" when {
      ".toString" must {
        "return URN" in {
          forAll(arbitrary[String]) {
            str =>
              val identifier: Identifier = URN(str)
              identifier.toString mustEqual "URN"
              s"$identifier" mustEqual "URN"
          }
        }
      }
    }
  }
}
