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
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import java.time.LocalDateTime

class NrsLockRepositorySpec extends AsyncFreeSpec with Matchers with IntegrationTestBase with BeforeAndAfterEach {

  private val identifier1: String = "1234567890"
  private val identifier2: String = "0987654321"

  private val internalId: String = "internalId"

  private val testDateTime: LocalDateTime = LocalDateTime.now()

  private val repository = createApplication.injector.instanceOf[NrsLockRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
  }

  "NrsLockRepository" - {

    "must be able to store and retrieve data" in  {

      await(repository.getLock(internalId, identifier1)) mustBe None
      await(repository.getLock(internalId, identifier2)) mustBe None

      val state1: NrsLock = NrsLock(locked = true, createdAt = testDateTime)
      await(repository.setLock(internalId, identifier1, state1)) mustBe true

      val state2: NrsLock = NrsLock(locked = false, createdAt = testDateTime)
      await(repository.setLock(internalId, identifier2, state2)) mustBe true

      await(repository.getLock(internalId, identifier1)) mustBe Some(state1)
      await(repository.getLock(internalId, identifier2)) mustBe Some(state2)
    }
  }
}
