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

import play.api.http.HeaderNames._
import models.auditing._
import models.requests.IdentifierRequest
import play.api.libs.json.JsValue
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import javax.inject.Inject
import models.FileDetails

import scala.concurrent.ExecutionContext

class AuditService @Inject() (auditConnector: AuditConnector, localDateTimeService: LocalDateTimeService)(implicit
  ec: ExecutionContext
) {

  def audit(event: String)(implicit request: IdentifierRequest[AnyContent], hc: HeaderCarrier): Unit = {

    val payload = ObligedEntityAuditEvent(
      internalAuthId = request.internalId,
      identifier = request.identifier.value,
      affinity = request.affinityGroup,
      dateTime = request.headers.get(DATE).getOrElse(localDateTimeService.now.toString)
    )

    auditConnector.sendExplicitAudit(event, payload)
  }

  def audit(event: String, response: JsValue)(implicit
    request: IdentifierRequest[AnyContent],
    hc: HeaderCarrier
  ): Unit = {

    val payload = ObligedEntityAuditResponseEvent(
      internalAuthId = request.internalId,
      identifier = request.identifier.value,
      affinity = request.affinityGroup,
      dateTime = request.headers.get(DATE).getOrElse(localDateTimeService.now.toString),
      response = response
    )

    auditConnector.sendExplicitAudit(event, payload)
  }

  def auditFileDetails(event: String, fileDetails: FileDetails)(implicit
    request: IdentifierRequest[AnyContent],
    hc: HeaderCarrier
  ): Unit = {

    val generationDateTime = localDateTimeService.now.toString
    val payload            = ObligedEntityAuditFileDetailsEvent(
      internalAuthId = request.internalId,
      identifier = request.identifier.value,
      affinity = request.affinityGroup,
      dateTime = request.headers.get(DATE).getOrElse(generationDateTime),
      fileName = fileDetails.fileName,
      fileType = fileDetails.fileType,
      fileSize = fileDetails.fileSize,
      fileGenerationDateTime = generationDateTime
    )

    auditConnector.sendExplicitAudit(event, payload)
  }

}
