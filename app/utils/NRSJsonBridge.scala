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

import play.api.Logging
import play.api.libs.json.{JsPath, JsValue, __}

object NRSJsonBridge {

  implicit class RichJsObject(in: JsValue) extends Logging {

    def transformForNRS: JsValue =
      JsonUppercase.uppercaseFieldsInArrayAtPath(
        JsonUppercase.uppercaseStringAtPath(
          in,
          objPersonNames*
        )(logger),
        allArrPersonNames*
      )(logger)

  }

  protected[utils] val arrPathsToUppercase: List[JsPath] =
    List(
      __ \ "success" \ "entities" \ "naturalPerson",
      __ \ "success" \ "entities" \ "beneficiary" \ "individual",
      __ \ "success" \ "entities" \ "protectors" \ "protectorIndividual",
      __ \ "success" \ "entities" \ "settlors" \ "settlorIndividual"
    )

  protected[utils] val objPathsToUppercase: List[JsPath] =
    List(
      __ \ "success" \ "entities" \ "leadTrustee" \ "leadTrusteeIndividual",
      __ \ "success" \ "entities" \ "settlors" \ "deceased"
    )

  private val fieldsToUppercase: List[JsPath] =
    List(
      __ \ "firstName",
      __ \ "middleName",
      __ \ "lastName"
    )

  private val objPersonNames: List[JsPath] =
    for {
      a <- objPathsToUppercase
      b <- fieldsToUppercase
    } yield a.compose(b)

  private val arrPersonNames: List[(JsPath, JsPath)] =
    for {
      a <- arrPathsToUppercase
      b <- fieldsToUppercase
    } yield (a, b)

  protected[utils] val trusteesPath: List[JsPath] = List(__ \ "success" \ "entities" \ "trustees")

  private val trusteesFieldsToUppercase: List[JsPath] =
    List(
      __ \ "trusteeIndividual" \ "firstName",
      __ \ "trusteeIndividual" \ "middleName",
      __ \ "trusteeIndividual" \ "lastName"
    )

  private val trusteesPersonNames: List[(JsPath, JsPath)] =
    for {
      a <- trusteesPath
      b <- trusteesFieldsToUppercase
    } yield (a, b)

  private val allArrPersonNames: List[(JsPath, JsPath)] =
    trusteesPersonNames ++ arrPersonNames

}
