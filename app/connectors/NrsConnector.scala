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

package connectors

import config.AppConfig
import config.Constants._
import javax.inject.Inject
import models.NrsResponse
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class NrsConnector @Inject()(http: HttpClient,
                             ws: WSClient,
                             config: AppConfig) {

  private lazy val nrsHeaders: Seq[(String, String)] = {
    Seq(
      X_API_KEY -> s"${config.nrsToken}",
      CONTENT_TYPE -> CONTENT_TYPE_JSON
    )
  }

  private lazy val url: String = s"${config.nrsUrl}/generate-pdf/template/trusts-5mld-1-0-0/signed-pdf"

  def getPdf(payload: JsValue)(implicit ec: ExecutionContext): Future[NrsResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = nrsHeaders)
    http.POST[JsValue, NrsResponse](url, payload)
  }

  def getPdfStreamed(payload: JsValue)(implicit ec: ExecutionContext): Future[NrsResponse] = {
    ws.url(url).withMethod(POST).withHttpHeaders(nrsHeaders: _*).withBody(payload).stream().map { response =>
      response.body[NrsResponse]
    }
  }

}
