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

package utils

import config.AppConfig
import helpers.ConnectorSpecHelper
import helpers.JsonHelper.getJsonValueFromFile
import play.api.Logging
import play.api.libs.json.JsValue
import services.ValidationService
import utils.NRSJsonBridge.{RichJsObject, arrPathsToUppercase, objPathsToUppercase, trusteesPath}

class NRSJsonBridgeSpec extends ConnectorSpecHelper with Logging {

  private val hipJson: JsValue = getJsonValueFromFile("valid-hip-with-lowercase-name-fields.json")

  private lazy val validationService: ValidationService = injector.instanceOf[ValidationService]

  private lazy val config: AppConfig = injector.instanceOf[AppConfig]

  private def validate(payload: JsValue) =
    validationService.get(config.nrsPdfGeneratorTrustSchema).validate(payload.toString)

  "NRSJsonBridge" when {

    "supplied json file " must {
      "contain all fields we need to uppercase" in
        (trusteesPath ++ arrPathsToUppercase ++ objPathsToUppercase).map { path =>
          hipJson.validateOpt(path.json.pick).get.nonEmpty
        }
    }

    "someJson.transformForNRS" must {
      "correctly convert first, middle and lastName fields to uppercase" in {

        val uppercaseJson = hipJson.transformForNRS

        assert(uppercaseJson.toString.contains("ADAM"))
        assert(!uppercaseJson.toString.contains("Adam"))
        assert(uppercaseJson.toString.contains("GEORGE"))
        assert(!uppercaseJson.toString.contains("George"))
        assert(uppercaseJson.toString.contains("ANT"))
        assert(!uppercaseJson.toString.contains("Ant"))
        assert(uppercaseJson.toString.contains("FIRSTNAME"))
        assert(!uppercaseJson.toString.contains("firstname"))
        assert(uppercaseJson.toString.contains("MIDDLENAME"))
        assert(!uppercaseJson.toString.contains("middlename"))
        assert(uppercaseJson.toString.contains("LASTNAME"))
        assert(!uppercaseJson.toString.contains("lastname"))
        assert(uppercaseJson.toString.contains("TITLECASE"))
        assert(!uppercaseJson.toString.contains("Titlecase"))

        assert(
          validate(NrsRequestMapper.toNrsRequest(uppercaseJson)).isRight
        )
      }
    }
  }

}
