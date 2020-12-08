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

package services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeService {

  def now: LocalDateTime = LocalDateTime.now

  def nowFormatted: String = {
    val format: String = "yyyy-MM-dd--HH-mm-ss"
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(format)
    now.format(dateFormatter)
  }

}
