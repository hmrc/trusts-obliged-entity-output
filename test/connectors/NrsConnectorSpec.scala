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

import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import config.Constants.CONTENT_LENGTH
import helpers.ConnectorSpecHelper
import helpers.JsonHelper._
import models._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class NrsConnectorSpec extends ConnectorSpecHelper {

  lazy val connector: NrsConnector = injector.instanceOf[NrsConnector]

  val url: String = "/generate-pdf/template/trusts-5mld-1-0-0/signed-pdf"

  val json: JsValue = getJsonValueFromFile("nrs-request-body.json")

  "NrsConnector" when {

    ".getPdf" must {

      "return SuccessfulResponse" when {
        "200 (OK) response received and there is a Content-Length header" in {

          val header: HttpHeaders = new HttpHeaders(
            new HttpHeader(CONTENT_LENGTH, "1887445")
          )

          stubForPost(server, url, Json.stringify(json), OK, "", header)

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe a[SuccessfulResponse]
          }
        }
      }

      "return BadRequestResponse" when {
        "400 (BAD_REQUEST) response received" in {

          stubForPost(server, url, Json.stringify(json), BAD_REQUEST, "")

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe BadRequestResponse
          }
        }
      }

      "return UnauthorisedResponse" when {
        "401 (UNAUTHORISED) response received" in {

          stubForPost(server, url, Json.stringify(json), UNAUTHORIZED, "")

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe UnauthorisedResponse
          }
        }
      }

      "return NotFoundResponse" when {
        "404 (NOT_FOUND) response received" in {

          stubForPost(server, url, Json.stringify(json), NOT_FOUND, "")

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe NotFoundResponse
          }
        }
      }

      "return InternalServerErrorResponse" when {

        "5xx response received" in {

          stubForPost(server, url, Json.stringify(json), INTERNAL_SERVER_ERROR, "")

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe InternalServerErrorResponse
          }
        }

        "200 (OK) response received but no Content-Length header" in {

          stubForPost(server, url, Json.stringify(json), OK, "")

          whenReady(connector.getPdf(json)) {
            response =>
              response mustBe InternalServerErrorResponse
          }
        }
      }
    }
  }
}
