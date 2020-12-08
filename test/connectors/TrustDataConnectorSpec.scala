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

import helpers.ConnectorSpecHelper
import helpers.JsonHelper._
import models._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class TrustDataConnectorSpec extends ConnectorSpecHelper {

  private lazy val connector: TrustDataConnector = injector.instanceOf[TrustDataConnector]

  val identifier = UTR("1234567890")

  private val url: String = s"/trusts/obliged-entities/$identifier/${identifier.value}"

  private val json: JsValue = getJsonValueFromFile("nrs-request-body.json")

  "TrustDataConnector" when {

    ".getTrustJson" must {

      "return a JsValue" when {
        "200 (OK) response received" in {

          stubForGet(url = url, returnStatus = OK, responseBody = Json.stringify(json))

          whenReady(connector.getTrustJson(identifier)) {
            response =>
              response mustEqual json
          }
        }
      }
//
//      "return BadRequestResponse" when {
//        "400 (BAD_REQUEST) response received" in {
//
//          stubForPost(url = url, requestBody = Json.stringify(json), returnStatus = BAD_REQUEST)
//
//          whenReady(connector.getPdf(json)) {
//            response =>
//              response mustBe BadRequestResponse
//          }
//        }
//      }
//
//      "return UnauthorisedResponse" when {
//        "401 (UNAUTHORISED) response received" in {
//
//          stubForPost(url = url, requestBody = Json.stringify(json), returnStatus = UNAUTHORIZED)
//
//          whenReady(connector.getPdf(json)) {
//            response =>
//              response mustBe UnauthorisedResponse
//          }
//        }
//      }
//
//      "return NotFoundResponse" when {
//        "404 (NOT_FOUND) response received" in {
//
//          stubForPost(url = url, requestBody = Json.stringify(json), returnStatus = NOT_FOUND)
//
//          whenReady(connector.getPdf(json)) {
//            response =>
//              response mustBe NotFoundResponse
//          }
//        }
//      }
//
//      "return InternalServerErrorResponse" when {
//
//        "5xx response received" in {
//
//          stubForPost(url = url, requestBody = Json.stringify(json), returnStatus = INTERNAL_SERVER_ERROR)
//
//          whenReady(connector.getPdf(json)) {
//            response =>
//              response mustBe InternalServerErrorResponse
//          }
//        }
//
//        "200 (OK) response received without a Content-Length header" in {
//
//          stubForPost(url = url, requestBody = Json.stringify(json), returnStatus = OK)
//
//          whenReady(connector.getPdf(json)) {
//            response =>
//              response mustBe InternalServerErrorResponse
//          }
//        }
//      }
    }
  }
}
