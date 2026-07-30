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

import helpers.ConnectorSpecHelper
import helpers.JsonHelper.*
import models.*
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}

class HipTrustDataConnectorSpec extends ConnectorSpecHelper {

  private lazy val connector: HipTrustDataConnector = injector.instanceOf[HipTrustDataConnector]

  private val utrIdentifier: Identifier = UTR("2134514321")
  private val urnIdentifier: Identifier = URN("XATRUST80000001")

  private def url(identifier: Identifier): String =
    s"/etmp/RESTAdapter/trustsandestates/obliged-entities/$identifier/${identifier.value}"

  private val hipJson: JsValue       = getJsonValueFromFile("valid-hip.json")
  private val hipSuccessBody: String = Json.stringify(hipJson)

  override def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .configure(
        Seq(
          "microservice.services.hip.obliged-entities.port" -> server.port()
        )*
      )

  "HipTrustDataConnector" when {

    ".getTrustJson" must {

      "return a SuccessfulHipTrustDataResponse with a Json payload" when {

        "a valid UTR is sent" in {

          stubForGet(url = url(utrIdentifier), responseStatus = OK, responseBody = hipSuccessBody)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe SuccessfulHipTrustDataResponse(hipJson)
          }
        }

        "a valid URN is sent" in {

          stubForGet(url = url(urnIdentifier), responseStatus = OK, responseBody = hipSuccessBody)

          whenReady(connector.getTrustJson(urnIdentifier)) { response =>
            response mustBe SuccessfulHipTrustDataResponse(hipJson)
          }
        }
      }

      "return InternalServerErrorTrustDataResponse" when {

        "200 OK with a body that fails HIP schema validation" in {

          stubForGet(
            url = url(utrIdentifier),
            responseStatus = OK,
            responseBody = Json.stringify(getJsonValueFromFile("nrs-request-body.json"))
          )

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe InternalServerErrorTrustDataResponse
          }
        }

        "500 response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = INTERNAL_SERVER_ERROR)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe InternalServerErrorTrustDataResponse
          }
        }

        "503 SERVICE_UNAVAILABLE response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = SERVICE_UNAVAILABLE)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe InternalServerErrorTrustDataResponse
          }
        }

        "422 with unparseable body" in {

          stubForGet(url = url(utrIdentifier), responseStatus = UNPROCESSABLE_ENTITY, responseBody = "{}")

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe InternalServerErrorTrustDataResponse
          }
        }
      }

      "return BadRequestTrustDataResponse" when {
        "400 response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = BAD_REQUEST)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe BadRequestTrustDataResponse
          }
        }
      }

      "return UnauthorisedTrustDataResponse" when {
        "401 response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = UNAUTHORIZED)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe UnauthorisedTrustDataResponse
          }
        }
      }

      "return ForbiddenTrustDataResponse" when {
        "403 response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = FORBIDDEN)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe ForbiddenTrustDataResponse
          }
        }
      }

      "return NotFoundTrustDataResponse" when {

        "404 response received" in {

          stubForGet(url = url(utrIdentifier), responseStatus = NOT_FOUND)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe NotFoundTrustDataResponse
          }
        }

        "422 with errorId 000" in {
          val body = Json
            .obj(
              "error" -> Json.obj(
                "processingDate" -> "2001-12-17T09:30:47.0",
                "errorId"        -> "000",
                "text"           -> "UTR or URN is invalid"
              )
            )
            .toString()

          stubForGet(url = url(utrIdentifier), responseStatus = UNPROCESSABLE_ENTITY, responseBody = body)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe NotFoundTrustDataResponse
          }
        }
      }

      "return UnprocessableEntityTrustDataResponse" when {

        "422 with errorId 003" in {
          val body = Json
            .obj(
              "error" -> Json.obj(
                "processingDate" -> "2001-12-17T09:30:47.0",
                "errorId"        -> "003",
                "text"           -> "Request could not be processed"
              )
            )
            .toString()

          stubForGet(url = url(utrIdentifier), responseStatus = UNPROCESSABLE_ENTITY, responseBody = body)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe UnprocessableEntityTrustDataResponse
          }
        }

        "422 with unknown errorId" in {
          val body = Json
            .obj(
              "error" -> Json.obj(
                "processingDate" -> "2001-12-17T09:30:47.0",
                "errorId"        -> "999",
                "text"           -> "Technical System Error"
              )
            )
            .toString()

          stubForGet(url = url(utrIdentifier), responseStatus = UNPROCESSABLE_ENTITY, responseBody = body)

          whenReady(connector.getTrustJson(utrIdentifier)) { response =>
            response mustBe UnprocessableEntityTrustDataResponse
          }
        }
      }
    }
  }

}
