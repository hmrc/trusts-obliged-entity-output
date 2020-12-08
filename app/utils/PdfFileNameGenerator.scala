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

import javax.inject.Inject
import play.api.libs.json.{JsPath, JsString, JsSuccess, JsValue}
import services.LocalDateTimeService

class PdfFileNameGenerator @Inject()(localDateTimeService: LocalDateTimeService) {

  def generate(payload: JsValue): Option[String] = {

    val maybeTrustName: Option[String] = {
      val path: JsPath = JsPath \ "trustName"
      payload.transform(path.json.pick) match {
        case JsSuccess(JsString(trustName), _) => Some(trustName.replaceAll(" ", "_"))
        case _ => None
      }
    }

    maybeTrustName match {
      case Some(trustName) =>
        val timestamp: String = localDateTimeService.nowFormatted
        Some(s"$trustName--$timestamp.pdf")
      case _ =>
        None
    }
  }
}
