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

package connectors

import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import play.api.http.HeaderNames._
import helpers.ConnectorSpecHelper
import helpers.JsonHelper._
import models._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class NrsConnectorSpec extends ConnectorSpecHelper {

  private lazy val connector: NrsConnector = injector.instanceOf[NrsConnector]

  "NrsConnector" when {

    ".getPdf" must {

      val url: String = "/generate-pdf/template/trusts-5mld-1-2-0/signed-pdf"

      val json: JsValue = getJsonValueFromFile("nrs-request-body.json")

      "return SuccessfulResponse" when {
        "200 (OK) response received with a Content-Length header" in {
          val headers: HttpHeaders = new HttpHeaders(
            new HttpHeader(CONTENT_LENGTH, "1887445")
          )

          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = OK, responseHeaders = headers)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe a[SuccessfulResponse]
          }
        }
      }

      "return BadRequestResponse" when {
        "400 (BAD_REQUEST) response received" in {
          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = BAD_REQUEST)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe BadRequestResponse
          }
        }
      }

      "return UnauthorisedResponse" when {
        "401 (UNAUTHORISED) response received" in {
          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = UNAUTHORIZED)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe UnauthorisedResponse
          }
        }
      }

      "return NotFoundResponse" when {
        "404 (NOT_FOUND) response received" in {
          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = NOT_FOUND)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe NotFoundResponse
          }
        }
      }

      "return InternalServerErrorResponse" when {

        "5xx response received" in {
          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = INTERNAL_SERVER_ERROR)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe InternalServerErrorResponse
          }
        }

        "200 (OK) response received without a Content-Length header" in {
          stubForPost(url = url, requestBody = Json.stringify(json), responseStatus = OK)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe InternalServerErrorResponse
          }
        }
      }
    }

    ".ping" must {

      val url: String = "/generate-pdf/ping"

      "return true" when {

        "200 (OK) response received" in {
          stubForGet(url = url, responseStatus = OK)

          whenReady(connector.ping()) {
            response =>
              response mustBe true
          }
        }
      }

      "return false" when {
        
        "non-200 response received" in {
          stubForGet(url = url, responseStatus = INTERNAL_SERVER_ERROR)

          whenReady(connector.ping()) {
            response =>
              response mustBe false
          }
        }
      }
    }
  }
}
