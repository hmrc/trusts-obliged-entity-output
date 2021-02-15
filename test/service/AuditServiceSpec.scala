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

package service

import base.SpecBase
import models.auditing.ObligedEntityAuditEvent
import models.requests.IdentifierRequest
import models.{Identifier, URN, UTR}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import play.api.mvc.{AnyContent, Headers}
import play.api.test.FakeRequest
import services.{AuditService, LocalDateTimeService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDateTime

class AuditServiceSpec extends SpecBase {

  private val auditConnector: AuditConnector = mock[AuditConnector]
  private val mockLocalDateTimeService: LocalDateTimeService = mock[LocalDateTimeService]
  private val auditService: AuditService = new AuditService(auditConnector, mockLocalDateTimeService)

  private val event: String = "event"
  private val internalId: String = "internalId"
  private val sessionId: String = "sessionId"

  "Audit service" must {

    "build audit payload from request values" when {

      "date header; agent affinity; utr identifier" in {

        reset(auditConnector)

        val date: String = "Wed, 16 Oct 2019 07:28:00 GMT"
        val headers: Headers = Headers(("Date", date))

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)

        val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest().withHeaders(headers), internalId, identifier, sessionId, affinity)

        auditService.audit(event)(request, hc)

        val expectedPayload = ObligedEntityAuditEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          sessionId = sessionId,
          dateTime = date
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }

      "no date header; org affinity; urn identifier" in {

        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenReturn(LocalDateTime.parse(date))

        val affinity: AffinityGroup = Organisation

        val urn: String = "urn"
        val identifier: Identifier = URN(urn)

        val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), internalId, identifier, sessionId, affinity)

        auditService.audit(event)(request, hc)

        val expectedPayload = ObligedEntityAuditEvent(
          internalAuthId = internalId,
          identifier = urn,
          affinity = affinity,
          sessionId = sessionId,
          dateTime = date
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }
    }
  }

}
