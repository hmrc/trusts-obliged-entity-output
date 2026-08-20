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

package service

import config.AppConfig
import connectors.{HipTrustDataConnector, IfsTrustDataConnector}
import helpers.JsonHelper.*
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.JsValue
import services.TrustDataServiceImpl

import scala.concurrent.{ExecutionContext, Future}

class TrustDataServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with BeforeAndAfter {

  private given ExecutionContext = ExecutionContext.global

  private val mockIfsTrustDataConnector: IfsTrustDataConnector = mock(classOf[IfsTrustDataConnector])
  private val mockHipTrustDataConnector: HipTrustDataConnector = mock(classOf[HipTrustDataConnector])
  private val mockAppConfig: AppConfig                         = mock(classOf[AppConfig])

  private val identifier: Identifier = UTR("2134514321")
  private val json: JsValue          = getJsonValueFromFile("nrs-request-body.json")
  private val hipJson: JsValue       = getJsonValueFromFile("valid-hip.json")

  private def service: TrustDataServiceImpl =
    new TrustDataServiceImpl(mockIfsTrustDataConnector, mockHipTrustDataConnector, mockAppConfig)

  before {
    reset(mockIfsTrustDataConnector, mockHipTrustDataConnector, mockAppConfig)
  }

  "TrustDataService" when {

    "features.hip.obligedEntities is false" must {

      "delegate to the IFS connector" in {
        when(mockAppConfig.useHipObligedEntities).thenReturn(false)
        when(mockIfsTrustDataConnector.getTrustJson(any()))
          .thenReturn(Future.successful(SuccessfulIfsTrustDataResponse(json)))

        whenReady(service.getTrustJson(identifier)) { response =>
          response mustBe SuccessfulIfsTrustDataResponse(json)
          verify(mockIfsTrustDataConnector).getTrustJson(identifier)
          verifyNoInteractions(mockHipTrustDataConnector)
        }
      }
    }

    "features.hip.obligedEntities is true" must {

      "delegate to the HIP connector" in {
        when(mockAppConfig.useHipObligedEntities).thenReturn(true)
        when(mockHipTrustDataConnector.getTrustJson(any()))
          .thenReturn(Future.successful(SuccessfulHipTrustDataResponse(hipJson)))

        whenReady(service.getTrustJson(identifier)) { response =>
          response mustBe SuccessfulHipTrustDataResponse(hipJson)
          verify(mockHipTrustDataConnector).getTrustJson(identifier)
          verifyNoInteractions(mockIfsTrustDataConnector)
        }
      }
    }
  }

}
