/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.Mockito.{mock, when}
import services.LocalDateTimeService

class PdfFileNameGeneratorSpec extends SpecBase {

  private val mockLocalDateTimeService: LocalDateTimeService = mock(classOf[LocalDateTimeService])
  when(mockLocalDateTimeService.nowFormatted).thenReturn("2020-04-01--09-30-00")
  private val pdfFileNameGenerator: PdfFileNameGenerator     = new PdfFileNameGenerator(mockLocalDateTimeService)

  "PdfFileNameGenerator" when {

    ".generate" must {

      "generate file name with trust identifier and timestamp" in {

        val identifier = "1234567890"

        pdfFileNameGenerator.generate(identifier) mustBe "1234567890-2020-04-01--09-30-00.pdf"
      }
    }
  }

}
