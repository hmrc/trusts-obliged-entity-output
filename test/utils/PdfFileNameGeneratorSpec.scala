/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.JsonHelper.getJsonValueFromFile
import helpers.LocalDateTimeServiceHelper
import play.api.libs.json.{JsValue, Json}

class PdfFileNameGeneratorSpec extends SpecBase {

  private val localDateService: LocalDateTimeServiceHelper = new LocalDateTimeServiceHelper()
  private val pdfFileNameGenerator: PdfFileNameGenerator = new PdfFileNameGenerator(localDateService)

  "PdfFileNameGenerator" when {

    ".generate" when {
      "payload has trust name" must {
        "generate file name with trust name and timestamp" in {

          val payload: JsValue = getJsonValueFromFile("nrs-request-body.json")

          pdfFileNameGenerator.generate(payload) mustEqual "TRUST_NAME--2020-04-01--09-30-00.pdf"
        }
      }

      "payload does not have trust name" must {
        "generate file name with timestamp" in {

          pdfFileNameGenerator.generate(Json.obj()) mustEqual "2020-04-01--09-30-00.pdf"
        }
      }
    }
  }
}
