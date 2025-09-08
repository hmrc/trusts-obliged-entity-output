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

package services

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}

import play.api.Logging
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.io.Source
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.{Failure, Success, Try}

@Singleton
class ValidationService @Inject()() {

  private val factory = JsonSchemaFactory.byDefault()

  def get(schemaFile: String): Validator = {
    val source = Source.fromFile(getClass.getResource(schemaFile).getPath)
    val schemaJsonFileString = source.mkString
    source.close()
    val schemaJson = JsonLoader.fromString(schemaJsonFileString)
    val schema = factory.getJsonSchema(schemaJson)
    new Validator(schema)
  }

}

class Validator(schema: JsonSchema) extends Logging {

  private val JsonErrorMessageTag = "message"
  private val JsonErrorInstanceTag = "instance"
  private val JsonErrorPointerTag = "pointer"

  def validate(inputJson: String): Either[List[TrustsValidationError], Unit] = {
    Try(JsonLoader.fromString(inputJson)) match {
      case Success(json) =>
        val result = schema.validate(json)

        if (result.isSuccess) {
          Right(())
        } else {
          logger.error(s"[Validator][validate] unable to validate to schema")
          val errors = getValidationErrors(result)
          Left(errors)
        }
      case Failure(e) =>
        logger.error(s"[Validator][validate] IOException $e")
        Left(List(TrustsValidationError(s"[Validator][validate] IOException $e", "")))
    }

  }

  private def getValidationErrors(validationOutput: ProcessingReport): List[TrustsValidationError] = {
    validationOutput.iterator.asScala.toList.filter(m => m.getLogLevel == ERROR).map(m => {
      val error = m.asJson()
      val message = error.findValue(JsonErrorMessageTag).asText("")
      val location = error.findValue(JsonErrorInstanceTag).at(s"/$JsonErrorPointerTag").asText()
      val locations = error.findValues(JsonErrorPointerTag)
      logger.error(s"[Validator][getValidationErrors] validation failed at locations :  $locations")
      TrustsValidationError(message, location)
    })
  }

}

case class TrustsValidationError(message: String, location: String)

object TrustsValidationError {
  implicit val formats: Format[TrustsValidationError] = Json.format[TrustsValidationError]
}
