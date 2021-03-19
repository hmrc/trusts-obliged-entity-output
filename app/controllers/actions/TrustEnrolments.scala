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

package controllers.actions

import uk.gov.hmrc.auth.core.Enrolments

object TrustEnrolments {

  implicit class GetIdentifierForTrustEnrolment(enrolments: Enrolments) {

    def trustIdentifier : Option[String] = {
      val utrEnrolment = enrolments.enrolments
        .find(_.key equals "HMRC-TERS-ORG")
        .flatMap(_.identifiers.find(_.key equals "SAUTR"))
        .map(_.value)

      val urnEnrolment = enrolments.enrolments
        .find(_.key equals "HMRC-TERSNT-ORG")
        .flatMap(_.identifiers.find(_.key equals "URN"))
        .map(_.value)

      utrEnrolment.orElse(urnEnrolment)
    }

  }

}