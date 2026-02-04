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

package repositories

import base.SpecBase
import models.NrsLock
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global

class NrsLockRepositorySpec extends SpecBase with MongoSupport with BeforeAndAfterEach {

  private val repository = new NrsLockRepository(mongoComponent, appConfig)

  private val identifier1: String = "1234567890"
  private val identifier2: String = "0987654321"

  private val internalId: String = "internalId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
  }

  "NrsLockRepository" should {

    "must be able to store and retrieve data" in {

      await(repository.getLock(internalId, identifier1)) mustBe false
      await(repository.getLock(internalId, identifier2)) mustBe false

      val state1: NrsLock = NrsLock.build(internalId, identifier1, locked = true)
      await(repository.setLock(state1)) mustBe true

      val state2: NrsLock = NrsLock.build(internalId, identifier2, locked = false)
      await(repository.setLock(state2)) mustBe true

      await(repository.collection.countDocuments(BsonDocument()).toFuture()) mustBe 2
      await(repository.getLock(internalId, identifier1))                     mustBe state1.locked
      await(repository.getLock(internalId, identifier2))                     mustBe state2.locked
    }

    "must be able to update data" in {

      await(repository.getLock(internalId, identifier1)) mustBe false
      await(repository.getLock(internalId, identifier2)) mustBe false

      val state1: NrsLock = NrsLock.build(internalId, identifier1, locked = true)
      await(repository.setLock(state1)) mustBe true

      val state2: NrsLock = NrsLock.build(internalId, identifier2, locked = false)
      await(repository.setLock(state2)) mustBe true

      val state1Update: NrsLock = NrsLock.build(internalId, identifier1, locked = false)
      await(repository.setLock(state1Update)) mustBe true

      val state2Update: NrsLock = NrsLock.build(internalId, identifier2, locked = true)
      await(repository.setLock(state2Update)) mustBe true

      await(repository.collection.countDocuments(BsonDocument()).toFuture()) mustBe 2
      await(repository.getLock(internalId, identifier1))                     mustBe state1Update.locked
      await(repository.getLock(internalId, identifier2))                     mustBe state2Update.locked
    }
  }

}
