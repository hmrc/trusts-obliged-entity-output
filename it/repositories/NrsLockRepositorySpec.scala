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

package repositories

import base.IntegrationTestBase
import models.NrsLock
import org.scalatest.{AsyncFreeSpec, MustMatchers}

class NrsLockRepositorySpec extends AsyncFreeSpec with MustMatchers with IntegrationTestBase {

  val identifier1 = "1234567890"
  val identifier2 = "0987654321"

  "the NrsLockRepository" - {

    "must be able to store and retrieve data" in assertMongoTest(createApplication) { app =>
      val repository = app.injector.instanceOf[NrsLockRepository]

      repository.getLock(identifier1).futureValue mustBe None
      repository.getLock(identifier2).futureValue mustBe None

      val state1 = NrsLock(true)
      repository.setLock(identifier1, state1).futureValue mustBe true

      val state2 = NrsLock(false)
      repository.setLock(identifier2, state2).futureValue mustBe true

      repository.getLock(identifier1).futureValue mustBe Some(state1)
      repository.getLock(identifier2).futureValue mustBe Some(state2)

    }
  }
}
