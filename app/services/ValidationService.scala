/*
 * Copyright 2026 HM Revenue & Customs
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

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.path.PathType
import com.networknt.schema.{Error as SchemaError, Schema, SchemaRegistry, SchemaRegistryConfig, SpecificationVersion}

import play.api.Logging
import play.api.libs.json.*

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

@Singleton
class ValidationService @Inject() () {

  private val mapper = new ObjectMapper()

  private val registry: SchemaRegistry = {
    val config = SchemaRegistryConfig
      .builder()
      .pathType(PathType.JSON_POINTER)
      .build()
    SchemaRegistry
      .builder()
      .defaultDialectId(SpecificationVersion.DRAFT_4.getDialectId)
      .schemaRegistryConfig(config)
      .build()
  }

  def get(schemaFile: String): Validator = {
    val schemaStream = getClass.getResourceAsStream(schemaFile)
    val schema       = registry.getSchema(schemaStream)
    new Validator(schema, mapper)
  }

}

class Validator(schema: Schema, mapper: ObjectMapper) extends Logging {

  def validate(inputJson: String): Either[List[TrustsValidationError], Unit] =
    Try(mapper.readTree(inputJson)) match {
      case Success(json) =>
        val errors = schema.validate(json).asScala.toList
        if (errors.isEmpty) {
          Right(())
        } else {
          logger.error(s"[Validator][validate] unable to validate to schema")
          Left(errors.map(toTrustsValidationError))
        }
      case Failure(e)    =>
        logger.error(s"[Validator][validate] IOException $e")
        Left(List(TrustsValidationError(s"[Validator][validate] IOException $e", "")))
    }

  private def toTrustsValidationError(error: SchemaError): TrustsValidationError = {
    val location = error.getInstanceLocation.toString
    logger.error(s"[Validator][getValidationErrors] validation failed at location: $location")
    TrustsValidationError(error.getMessage, location)
  }

}

case class TrustsValidationError(message: String, location: String)

object TrustsValidationError {
  given formats: Format[TrustsValidationError] = Json.format[TrustsValidationError]
}
