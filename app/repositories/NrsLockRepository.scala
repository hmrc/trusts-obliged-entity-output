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

import config.AppConfig
import models.NrsLock
import play.api.Logging
import play.api.libs.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class NrsLockRepository @Inject()(mongo: MongoDriver,
                                  config: AppConfig)
                                 (implicit ec: ExecutionContext) extends Logging {

  private val collectionName: String = "nrs-lock"

  private val lockTtl: Int = config.lockTtlInSeconds

  private def collection: Future[JSONCollection] =
    for {
      _ <- ensureIndexes
      res <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
    } yield res

  private val createdAtIndex: Index = Index(
    key = Seq("createdAt" -> IndexType.Ascending),
    name = Some("created-at-index"),
    options = BSONDocument("expireAfterSeconds" -> lockTtl)
  )

  private lazy val ensureIndexes: Future[Boolean] = {
    logger.info("Ensuring collection indexes")
    for {
      collection <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      createdIndex <- collection.indexesManager.ensure(createdAtIndex)
    } yield createdIndex
  }

  def setLock(identifier: String, lock: NrsLock): Future[Boolean] = {

    val selector = Json.obj(
      "identifier" -> identifier
    )

    val modifier = Json.obj(
      "$set" -> lock
    )

    collection.flatMap(_.update(
      ordered = false
    ).one(
      q = selector,
      u = modifier,
      upsert = true
    ).map(_.ok))
  }

  def getLock(identifier: String): Future[Option[NrsLock]] = {

    val selector = Json.obj(
      "identifier" -> identifier
    )

    collection.flatMap(_.find(
      selector = selector,
      projection = None
    ).one[NrsLock])
  }

}
