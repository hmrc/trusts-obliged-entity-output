/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.Mockito.{mock, reset, when}
import services.LocalDateTimeService

import java.time.LocalDateTime

class LocalDateTimeServiceSpec extends SpecBase {

  private val localDateTimeService: LocalDateTimeService = mock(classOf[LocalDateTimeService])

  "LocalDateTimeService" when {

    ".nowFormatted" when {

      "am" in {

        reset(localDateTimeService)
        when(localDateTimeService.now).thenReturn(LocalDateTime.of(1996, 2, 3, 9, 15, 30))
        when(localDateTimeService.nowFormatted).thenCallRealMethod()

        localDateTimeService.nowFormatted mustEqual "1996-02-03--09-15-30"
      }

      "pm" in {

        reset(localDateTimeService)
        when(localDateTimeService.now).thenReturn(LocalDateTime.of(1996, 2, 3, 17, 15, 30))
        when(localDateTimeService.nowFormatted).thenCallRealMethod()

        localDateTimeService.nowFormatted mustEqual "1996-02-03--17-15-30"
      }
    }
  }

}
