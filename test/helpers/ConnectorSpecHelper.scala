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

package helpers

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.Constants._
import controllers.Assets.{CONTENT_TYPE, JSON}
import org.scalatest.concurrent.IntegrationPatience
import play.api.inject.guice.GuiceApplicationBuilder

class ConnectorSpecHelper extends SpecBase with WireMockHelper with IntegrationPatience {

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "microservice.services.nrs-trusts.port" -> server.port(),
          "microservice.services.trust-data.port" -> server.port(),
          "pdf-test-endpoint-enabled" -> false
        ): _*
      )
  }

  def stubForGet(url: String,
                 returnStatus: Int,
                 responseBody: String,
                 delayResponse: Int = 0): StubMapping = {
    server.stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(returnStatus)
          .withBody(responseBody).withFixedDelay(delayResponse)))
  }

  def stubForPost(url: String,
                  requestBody: String,
                  returnStatus: Int,
                  responseBody: String = "",
                  responseHeaders: HttpHeaders = HttpHeaders.noHeaders()): StubMapping = {

    server.stubFor(post(urlEqualTo(url))
      .withHeader(X_API_KEY, equalTo(appConfig.nrsToken))
      .withHeader(CONTENT_TYPE, equalTo(JSON))
      .withRequestBody(equalTo(requestBody))
      .willReturn(
        aResponse()
          .withStatus(returnStatus)
          .withHeaders(responseHeaders)
          .withBody(responseBody)
          .withFixedDelay(0)
      )
    )
  }

}
