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

package service

import base.SpecBase
import connectors.TrustAuthConnector
import models.requests.IdentifierRequest
import models.{TrustAuthAllowed, TrustAuthDenied, TrustAuthInternalServerError, UTR}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthenticationService
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import scala.concurrent.Future

class AuthenticationServiceSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  private val utr = "0987654321"

  private implicit val request: IdentifierRequest[AnyContent]
  = IdentifierRequest[AnyContent](FakeRequest(), "internalId", UTR(utr), "sessionId", Organisation)

  private lazy val trustAuthConnector = mock(classOf[TrustAuthConnector])

  "invoking authenticateForIdentifier" when {

    "user is authenticated" must {
      "return the data request" in {
        when(trustAuthConnector.authorisedForIdentifier(any())(any(), any())).thenReturn(Future.successful(TrustAuthAllowed()))

        val app = buildApp

        val service = app.injector.instanceOf[AuthenticationService]

        whenReady(service.authenticateForIdentifier[AnyContent](utr)) {
          result =>
            result.value mustBe request
        }
      }
    }

    "user requires additional action" must {

      "return unauthorised" in {
        when(trustAuthConnector.authorisedForIdentifier(any())(any(), any())).thenReturn(Future.successful(TrustAuthDenied("some-url")))

        val app = buildApp

        val service = app.injector.instanceOf[AuthenticationService]

        whenReady(service.authenticateForIdentifier[AnyContent](utr)) {
          result =>
            result.left.value.header.status mustBe UNAUTHORIZED
        }
      }
    }

    "an internal server error is returned" must {

      "return an internal server error result" in {
        when(trustAuthConnector.authorisedForIdentifier(any())(any(), any())).thenReturn(Future.successful(TrustAuthInternalServerError))

        val app = buildApp

        val service = app.injector.instanceOf[AuthenticationService]

        whenReady(service.authenticateForIdentifier[AnyContent](utr)) {
          result =>
            result.left.value.header.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

  }

  private def buildApp = {
    applicationBuilder()
      .overrides(bind[TrustAuthConnector].toInstance(trustAuthConnector))
      .build()
  }
}
