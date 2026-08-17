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

package connectors

import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.UUID
import config.AppConfig
import config.Constants.{HIP_CORRELATION_ID, X_ORIGINATING_SYSTEM, X_RECEIPT_DATE, X_TRANSMITTING_SYSTEM}

import javax.inject.Inject
import models.*
import play.api.Logging
import play.api.http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class HipTrustDataConnector @Inject() (http: HttpClientV2, config: AppConfig)(using ec: ExecutionContext)
    extends TrustDataConnector with Logging {

  private val receiptDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

  private def hipHeaders(correlationId: String): Seq[(String, String)] =
    Seq(
      HIP_CORRELATION_ID        -> correlationId,
      X_ORIGINATING_SYSTEM      -> "TRS",
      X_RECEIPT_DATE            -> receiptDateFormatter.format(Instant.now()),
      X_TRANSMITTING_SYSTEM     -> "HIP",
      HeaderNames.AUTHORIZATION -> s"Basic ${config.hipAuthorizationToken}"
    )

  def getTrustJson(identifier: Identifier): Future[TrustDataResponse] = {
    lazy val url: String =
      s"${config.hipObligedEntitiesUrl}/etmp/RESTAdapter/trustsandestates/obliged-entities/$identifier/${identifier.value}"

    val correlationId       = UUID.randomUUID().toString
    given hc: HeaderCarrier = HeaderCarrier(authorization = None, extraHeaders = hipHeaders(correlationId))
    logger.info(s"[Session ID: ${Session.id(hc)}] getTrustJson correlationId: $correlationId from call to url: $url")

    http
      .get(url"$url")
      .execute[TrustDataResponse](using TrustDataResponse.hipHttpReads(identifier), ec)
  }

}
