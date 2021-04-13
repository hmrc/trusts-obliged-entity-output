/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.Assets.DATE
import models.auditing.{ObligedEntityAuditEvent, ObligedEntityAuditFileDetailsEvent}
import models.requests.IdentifierRequest
import play.api.libs.json.JsValue
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import javax.inject.Inject
import models.FileDetails

import scala.concurrent.ExecutionContext.Implicits._

class AuditService @Inject()(auditConnector: AuditConnector,
                             localDateTimeService: LocalDateTimeService) {

  def audit(event: String,
            response: Option[JsValue] = None,
           )(implicit request: IdentifierRequest[AnyContent], hc: HeaderCarrier): Unit = {

    val payload = ObligedEntityAuditEvent(
      internalAuthId = request.internalId,
      identifier = request.identifier.value,
      affinity = request.affinityGroup,
      dateTime = request.headers.get(DATE).getOrElse(localDateTimeService.now.toString),
      response = response
    )

    auditConnector.sendExplicitAudit(event, payload)
  }

  def auditFileDetails(event: String,
            fileDetails: FileDetails
           )(implicit request: IdentifierRequest[AnyContent], hc: HeaderCarrier): Unit = {

    val payload = ObligedEntityAuditFileDetailsEvent(
      internalAuthId = request.internalId,
      identifier = request.identifier.value,
      affinity = request.affinityGroup,
      dateTime = request.headers.get(DATE).getOrElse(localDateTimeService.now.toString),
      fileName = fileDetails.fileName,
      fileType = fileDetails.fileType,
      fileSize = fileDetails.fileSize,
      fileGenerationDateTime = localDateTimeService.now.toString,
    )

    auditConnector.sendExplicitAudit(event, payload)
  }
}
