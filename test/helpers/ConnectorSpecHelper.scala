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

package helpers

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.Constants._
import play.api.http.HeaderNames._
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.ContentTypes.JSON
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}

class ConnectorSpecHelper extends SpecBase with WireMockHelper with IntegrationPatience {

  val fakeCorrelationId: String = "bcda2e24-13c2-4fee-9243-cd9835426559"
  val fakeBearerToken: String = "Bearer 12345"

  val jsonResponse400CorrelationId: JsValue = Json.parse(
    s"""
       |{
       | "code": "INVALID_CORRELATIONID",
       | "reason": "Submission has not passed validation. Invalid CorrelationId."
       |}""".stripMargin)


  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "microservice.services.nrs-trusts.port" -> server.port(),
          "microservice.services.trust-data.port" -> server.port()
        ): _*
      )
  }

  def stubForGet(url: String, responseStatus: Int,
                 responseBody: String = "",
                 delayResponse: Int = 0): StubMapping = {

    server.stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(responseStatus)
          .withBody(responseBody).withFixedDelay(delayResponse)))
  }

  def stubForPost(url: String,
                  requestBody: String,
                  responseStatus: Int,
                  responseBody: String = "[]",
                  responseHeaders: HttpHeaders = HttpHeaders.noHeaders(),
                  delayResponse: Int = 0): StubMapping = {

    server.stubFor(post(urlEqualTo(url))
      .withHeader(X_API_KEY, equalTo(appConfig.nrsToken))
      .withHeader(CONTENT_TYPE, equalTo(JSON))
      .withRequestBody(equalTo(requestBody))
      .willReturn(
        aResponse()
          .withStatus(responseStatus)
          .withHeaders(responseHeaders)
          .withBody(responseBody)
          .withFixedDelay(delayResponse)
      )
    )
  }

}
