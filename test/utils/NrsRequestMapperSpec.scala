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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.JsObject
import utils.JsonUtils.getJsonValueFromFile

class NrsRequestMapperSpec extends AnyWordSpec with Matchers {

  "NrsRequestMapper.toNrsRequest" must {

    "map the HIP success payload to IFS shape expected by NRS" in {
      val hipJson = getJsonValueFromFile("valid-hip.json")

      val result = NrsRequestMapper.toNrsRequest(hipJson)

      (result \ "success").toOption                                                     mustBe None
      (result \ "entities" \ "beneficiary" \ "trust").toOption                          mustBe defined
      (result \ "entities" \ "beneficiary" \ "trusts").toOption                         mustBe None
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "name").as[String]  mustBe "A"
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "orgName").toOption mustBe None
    }

    "map a payload that is not wrapped in a success node" in {
      val unwrappedHipJson = (getJsonValueFromFile("valid-hip.json") \ "success").as[JsObject]

      val result = NrsRequestMapper.toNrsRequest(unwrappedHipJson)

      (result \ "entities" \ "beneficiary" \ "trust").toOption                          mustBe defined
      (result \ "entities" \ "beneficiary" \ "trusts").toOption                         mustBe None
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "name").as[String]  mustBe "A"
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "orgName").toOption mustBe None
    }
  }

}
