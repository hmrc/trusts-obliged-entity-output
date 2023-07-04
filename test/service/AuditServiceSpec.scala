/*
 * Copyright 2023 HM Revenue & Customs
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
import models.auditing.{ObligedEntityAuditEvent, ObligedEntityAuditFileDetailsEvent, ObligedEntityAuditResponseEvent}
import models.requests.IdentifierRequest
import models.{FileDetails, Identifier, URN, UTR}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, verify, when}
import play.api.http.HeaderNames
import play.api.libs.json.{JsNumber, JsString, JsValue}
import play.api.mvc.{AnyContent, Headers}
import play.api.test.{FakeHeaders, FakeRequest}
import services.{AuditService, LocalDateTimeService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDateTime

class AuditServiceSpec extends SpecBase {

  private val auditConnector: AuditConnector = mock(classOf[AuditConnector])
  private val mockLocalDateTimeService: LocalDateTimeService = mock(classOf[LocalDateTimeService])
  private val auditService: AuditService = new AuditService(auditConnector, mockLocalDateTimeService)

  private val event: String = "event"
  private val internalId: String = "internalId"
  private val sessionId: String = "sessionId"

  "Audit service" must {

    "build audit payload from request when no payload provided" when {

      "date header; agent affinity; utr identifier" in {

        reset(auditConnector)

        val date: String = "Wed, 16 Oct 2019 07:28:00 GMT"
        val headers: Headers = Headers((HeaderNames.DATE, date))

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)

        val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest().withHeaders(headers), internalId, identifier, sessionId, affinity)

        auditService.audit(event)(request, hc)

        val expectedPayload = ObligedEntityAuditEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
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
          dateTime = date
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }
    }

    "build audit payload from request when payload provided" when {

      "no date header; agent affinity; utr identifier" in {

        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenReturn(LocalDateTime.parse(date))

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)

        val response: JsValue = JsNumber(10.443335)
        val request: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), internalId, identifier, sessionId, affinity)

        auditService.audit(event, response)(request, hc)

        val expectedPayload = ObligedEntityAuditResponseEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          dateTime = date,
          response = response
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }

      "with date header; agent affinity; utr identifier" in {

        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenCallRealMethod()

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)
        val headers = FakeHeaders(Seq((HeaderNames.DATE, date)))
        val fakeRequest = FakeRequest().withHeaders(headers)

        val response: JsValue = JsNumber(10.443335)
        val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, identifier, sessionId, affinity)

        auditService.audit(event, response)(request, hc)

        val expectedPayload = ObligedEntityAuditResponseEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          dateTime = date,
          response = response
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }

      "with date header; agent affinity; utr identifier; string payload" in {

        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenCallRealMethod()

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)
        val headers = FakeHeaders(Seq((HeaderNames.DATE, date)))
        val fakeRequest = FakeRequest().withHeaders(headers)

        val response: JsValue = JsString("test")
        val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, identifier, sessionId, affinity)

        auditService.audit(event, response)(request, hc)

        val expectedPayload = ObligedEntityAuditResponseEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          dateTime = date,
          response = response
        )

        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }
    }

    "build audit by load from request and file" when {

      "with date header" in {
        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        val date2: String = "2022-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenReturn(LocalDateTime.parse(date2))

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)
        val headers = FakeHeaders(Seq((HeaderNames.DATE, date)))
        val fakeRequest = FakeRequest().withHeaders(headers)

        val fileDetails = FileDetails("abc", "def", 10L)

        val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, identifier, sessionId, affinity)

        auditService.auditFileDetails(event, fileDetails)(request, hc)

        val expectedPayload = ObligedEntityAuditFileDetailsEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          dateTime = date,
          fileName = fileDetails.fileName,
          fileType = fileDetails.fileType,
          fileSize = fileDetails.fileSize,
          fileGenerationDateTime = date2
        )
        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }

      "without date in header" in {
        reset(auditConnector)

        val date: String = "2021-01-01T09:30:15"
        when(mockLocalDateTimeService.now).thenReturn(LocalDateTime.parse(date))

        val affinity: AffinityGroup = Agent

        val utr: String = "utr"
        val identifier: Identifier = UTR(utr)
        val fakeRequest = FakeRequest()

        val fileDetails = FileDetails("abc", "def", 10L)

        val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, identifier, sessionId, affinity)

        auditService.auditFileDetails(event, fileDetails)(request, hc)

        val expectedPayload = ObligedEntityAuditFileDetailsEvent(
          internalAuthId = internalId,
          identifier = utr,
          affinity = affinity,
          dateTime = date,
          fileName = fileDetails.fileName,
          fileType = fileDetails.fileType,
          fileSize = fileDetails.fileSize,
          fileGenerationDateTime = date
        )
        verify(auditConnector).sendExplicitAudit(eqTo(event), eqTo(expectedPayload))(any(), any(), any())
      }
    }
  }
}
