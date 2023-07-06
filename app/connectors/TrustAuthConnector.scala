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

package connectors

import com.google.inject.ImplementedBy
import config.AppConfig
import javax.inject.Inject
import models.{TrustAuthInternalServerError, TrustAuthResponse}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[TrustAuthConnectorImpl])
trait TrustAuthConnector {
  def authorisedForIdentifier(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustAuthResponse]
}

class TrustAuthConnectorImpl @Inject()(http: HttpClient, config: AppConfig)
  extends TrustAuthConnector {

  val baseUrl: String = config.trustAuthUrl + "/trusts-auth"

  override def authorisedForIdentifier(identifier: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustAuthResponse] = {
    http.GET[TrustAuthResponse](s"$baseUrl/authorised/$identifier").recoverWith {
      case _ => Future.successful(TrustAuthInternalServerError)
    }
  }
}
