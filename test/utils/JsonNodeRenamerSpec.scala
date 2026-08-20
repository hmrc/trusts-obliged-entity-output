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
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

class JsonNodeRenamerSpec extends AnyWordSpec with Matchers {

  private given Logger = Logger(classOf[JsonNodeRenamerSpec])

  private val payload: JsObject = Json.obj(
    "trustName" -> "Trust",
    "entities"  -> Json.obj(
      "beneficiary" -> Json.obj(
        "trusts"  -> Json.arr(Json.obj("name" -> "A")),
        "charity" -> Json.arr(Json.obj("name" -> "B"))
      ),
      "leadTrustee" -> Json.obj(
        "leadTrusteeCompany" -> Json.obj("orgName" -> "C")
      )
    )
  )

  "JsonNodeRenamer.renameNode" must {

    "rename the target node and leave the rest of the payload untouched" in {
      val result = JsonNodeRenamer.renameNode(payload, "entities.beneficiary.trusts", "trust")

      (result \ "entities" \ "beneficiary" \ "trust").toOption  mustBe defined
      (result \ "entities" \ "beneficiary" \ "trusts").toOption mustBe None

      (result \ "trustName").as[String]                                                   mustBe "Trust"
      (result \ "entities" \ "beneficiary" \ "charity").as[Seq[JsObject]]                 mustBe
        (payload \ "entities" \ "beneficiary" \ "charity").as[Seq[JsObject]]
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "orgName").as[String] mustBe "C"
    }

    "rename a node nested deeper than the sibling branches" in {
      val result =
        JsonNodeRenamer.renameNode(payload, "entities.leadTrustee.leadTrusteeCompany.orgName", "name")

      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "name").as[String]  mustBe "C"
      (result \ "entities" \ "leadTrustee" \ "leadTrusteeCompany" \ "orgName").toOption mustBe None
      (result \ "entities" \ "beneficiary" \ "trusts").toOption                         mustBe defined
    }

    "return the payload unchanged when the path does not exist" in {
      JsonNodeRenamer.renameNode(payload, "entities.beneficiary.doesNotExist", "renamed") mustBe payload
    }

    "return the payload unchanged when the path has no parent node to rebuild" in {
      JsonNodeRenamer.renameNode(payload, "trustName", "renamed") mustBe payload
    }

    "return the payload unchanged when the path omits the success wrapper" in {
      val wrapped = Json.obj("success" -> payload)

      JsonNodeRenamer.renameNode(wrapped, "entities.beneficiary.trusts", "trust") mustBe wrapped
    }
  }

}
