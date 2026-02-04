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

import config.AppConfig
import models.NrsLock
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NrsLockRepository @Inject() (mongoComponent: MongoComponent, config: AppConfig)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[NrsLock](
      mongoComponent = mongoComponent,
      collectionName = "nrs-lock",
      domainFormat = NrsLock.format,
      replaceIndexes = true,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("createdAt"),
          IndexOptions()
            .name("created-at-index")
            .expireAfter(config.lockTtlInSeconds, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.ascending("identifier"),
          IndexOptions().name("identifier-index").unique(false)
        )
      )
    ) {

  def getLock(internalId: String, identifier: String): Future[Boolean] = {
    val selector = equal("identifier", s"$internalId~$identifier")

    collection.find(selector).headOption().map(_.exists(_.locked))
  }

  def setLock(lock: NrsLock): Future[Boolean] = {
    val selector = equal("identifier", s"${lock.identifier}")

    val options = new ReplaceOptions().upsert(true)
    collection.replaceOne(selector, lock, options).headOption().map(_.exists(_.wasAcknowledged()))
  }

}
