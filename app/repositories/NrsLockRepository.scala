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

package repositories

import config.AppConfig
import models.NrsLock
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class NrsLockRepository @Inject()(
                                   mongoComponent: MongoComponent,
                                   config: AppConfig
                                 )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[NrsLock](
    mongoComponent = mongoComponent,
    collectionName = "nrs-lock",
    domainFormat = NrsLock.format,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("createdAt"),
        IndexOptions()
          .name("created-at-index")
          .expireAfter(config.lockTtlInSeconds, TimeUnit.SECONDS))
    )
  ) {
  def getLock(internalId: String, identifier: String): Future[Option[NrsLock]] = {

    val selector = equal("identifier", s"$internalId~$identifier")

    collection.find(selector).headOption()
  }

  def setLock(internalId: String, identifier: String, lock: NrsLock): Future[Boolean] = {

    val selector = equal("identifier", s"$internalId~$identifier")

    val modifier = combine(
      set("identifier", s"$internalId~$identifier"),
      set("locked", toBson(lock.locked)),
      set("createdAt", toBson(lock.createdAt))
    )

    val updateOptions = new UpdateOptions().upsert(true)

    collection.updateOne(selector, modifier, updateOptions).headOption().map(_.exists(_.wasAcknowledged()))
  }
}
