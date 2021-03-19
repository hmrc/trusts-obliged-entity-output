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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthenticationService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  type RetrievalType = Option[String] ~ Option[AffinityGroup]

  private val cc = stubControllerComponents()

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockAuthService: AuthenticationService = mock[AuthenticationService]

  private val fakeRequest = FakeRequest("POST", "")
    .withHeaders(CONTENT_TYPE -> "application/json")
    .withBody(Json.parse("{}"))

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[JsValue] = authAction.apply(cc.parsers.json) { _ => Results.Ok }
  }

  private def authRetrievals(affinityGroup: AffinityGroup): Future[Some[String] ~ Some[AffinityGroup]] =
    Future.successful(new ~(Some("id"), Some(affinityGroup)))

  private def actionToTest(identifier: String, trustAuthService: AuthenticationService, authConnector: AuthConnector): AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(identifier, trustAuthService, authConnector)(injector.instanceOf[BodyParsers.Default], ExecutionContext.Implicits.global)

  "Auth Action" when {

    "the user hasn't logged in" must {

      "return unauthorised" in {

        val authAction = actionToTest("1234567890", mockAuthService, new FakeFailingAuthConnector(new MissingBearerToken))
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "the user's session has expired" must {

      "return unauthorised" in {

        val authAction = actionToTest("1234567890", mockAuthService, new FakeFailingAuthConnector(new BearerTokenExpired))
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "org user requests a PDF for UTR/URN that they're authenticated to see" must {

      "continue with the request" in {

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation))

        when(mockAuthService.authenticateForIdentifier[JsValue](any())(any(), any())).thenReturn(Future.successful(Right(fakeRequest)))

        val action = actionToTest("2647384758", mockAuthService, mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "agent user requests a PDF that they are authenticated to see" must {

      "continue with the request" in {

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Agent))

        when(mockAuthService.authenticateForIdentifier[JsValue](any())(any(), any())).thenReturn(Future.successful(Right(fakeRequest)))

        val action = actionToTest("XTTRUST80837546", mockAuthService, mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "org user requests a PDF for UTR/URN that does not match their enrolment" must {

      "return unauthorised" in {

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation))

        when(mockAuthService.authenticateForIdentifier[JsValue](any())(any(), any())).thenReturn(Future.successful(Left(Unauthorized)))

        val action = actionToTest("XTTRUST80837546", mockAuthService, mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }

    "agent org user requests a PDF for UTR/URN that does not match their enrolment" must {

      "return unauthorised" in {

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Agent))

        when(mockAuthService.authenticateForIdentifier[JsValue](any())(any(), any())).thenReturn(Future.successful(Left(Unauthorized)))

        val action = actionToTest("0043748273", mockAuthService, mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }

    "user requests a PDF for UTR/URN that is not a valid identifier" must {

      "return internal server error" in {

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation))

        val action = actionToTest("84759873598738597397598", mockAuthService, mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    Future.failed(exceptionToReturn)
  }
  
}

class FakeAuthConnector(stubbedRetrievalResult: Future[_]) extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    stubbedRetrievalResult.map(_.asInstanceOf[A])
  }

}
