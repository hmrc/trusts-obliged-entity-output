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

package service

import base.SpecBase
import org.scalatest.EitherValues
import services.{ValidationService, Validator}
import utils.JsonUtils

class ValidationServiceSpec extends SpecBase with EitherValues {

  private lazy val validationService: ValidationService = new ValidationService()

  private lazy val trustIfsValidator: Validator =
    validationService.get("/resources/schemas/ifs/get-trust-obliged-entities-data-schema-v1.2.0.json")

  private lazy val trustHipValidator: Validator =
    validationService.get(
      "/resources/schemas/hip/ObligedEntitiesSuccessResponse_EPID1755_TRS_openapi_v0.1.3.json"
    )

  "ValidationService " should {
    "return an empty list of errors when " when {
      "Ifs Json having all required fields" in {
        val jsonString = JsonUtils.getJsonFromFile("valid-ifs.json")

        trustIfsValidator.validate(jsonString).isRight mustBe true
      }

      "HIP Json having all required fields" in {
        val jsonString = JsonUtils.getJsonFromFile("valid-hip.json")

        trustHipValidator.validate(jsonString).isRight mustBe true
      }
    }

    "return errors" when {
      "the Ifs json is invalid" in {
        val jsonString = JsonUtils.getJsonFromFile("invalid-ifs.json")

        val result = trustIfsValidator.validate(jsonString)
        result.isLeft                   mustBe true
        result.left.value.head.location mustBe "/correspondence/address"
      }

      "the HIP json is invalid" in {
        val jsonString = JsonUtils.getJsonFromFile("invalid-hip.json")

        val result = trustHipValidator.validate(jsonString)
        result.isLeft                   mustBe true
        result.left.value.head.location mustBe "/success/correspondence/address"
      }
    }
  }

}
