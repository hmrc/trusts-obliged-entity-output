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

import play.api.Logger
import play.api.libs.json.*

import scala.annotation.tailrec

object JsonUppercase {

  private def uppercaseTransformer(path: JsPath): Reads[JsObject] =
    path.json.update(__.read[String].map(str => JsString(str.toUpperCase)))

  private def uppercaseTransformerFieldsInArray(path: JsPath, field: JsPath): Reads[JsObject] =
    path.json
      .update(
        Reads
          .list(
            field.json.update(__.read[String].map(str => JsString(str.toUpperCase))).orElse(__.read[JsObject])
          )
          .map(JsArray(_))
      )
      .orElse(__.read[JsObject])

  @tailrec
  def uppercaseFieldsInArrayAtPath(json: JsValue, paths: (JsPath, JsPath)*)(implicit logger: Logger): JsValue =
    paths match {
      case Nil    => json
      case h :: t =>
        uppercaseFieldsInArrayAtPath(
          json.transform(uppercaseTransformerFieldsInArray(h._1, h._2)).getOrElse(json),
          t*
        )
    }

  @tailrec
  def uppercaseStringAtPath(json: JsValue, paths: JsPath*)(implicit logger: Logger): JsValue = paths match {
    case Nil    => json
    case h :: t =>
      uppercaseStringAtPath(
        json.transform(uppercaseTransformer(h)).getOrElse {
          logger.warn(
            s"path $h not found when trying to uppercase a value, probably the field is not required, " +
              s"check your use case to see if you think it should be there"
          )
          json
        },
        t*
      )
  }

}
