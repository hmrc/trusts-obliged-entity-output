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
import play.api.mvc.{Action, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  type RetrievalType = Option[String] ~ Option[AffinityGroup] ~ Enrolments

  private val cc = stubControllerComponents()

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private val fakeRequest = FakeRequest("POST", "")
    .withHeaders(CONTENT_TYPE -> "application/json")
    .withBody(Json.parse("{}"))

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[JsValue] = authAction.apply(cc.parsers.json) { _ => Results.Ok }
  }

  private def authRetrievals(affinityGroup: AffinityGroup, enrolments: Enrolments): Future[Some[String] ~ Some[AffinityGroup] ~ Enrolments] =
    Future.successful(new ~(new ~(Some("id"), Some(affinityGroup)), enrolments))

  private def actionToTest(identifier: String, authConnector: AuthConnector): AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(identifier, authConnector)(injector.instanceOf[BodyParsers.Default], ExecutionContext.Implicits.global)

  "Auth Action" when {

    "Agent user with no delegated enrolments" must {

      "return unauthorised" in {

        val enrolments = Enrolments(Set())

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val authAction = actionToTest("1234567890", mockAuthConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "Org user with no enrolments" must {

      "return unauthorised" in {

        val enrolments = Enrolments(Set())

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val authAction = actionToTest("1234567890", mockAuthConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "Individual user" must {

      "return unauthorised" in {

        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-NI", Seq(EnrolmentIdentifier("NINO", "JP121212A")), "Activated", None)
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Individual, enrolments))

        val authAction = actionToTest("1234567890", mockAuthConnector)

        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe UNAUTHORIZED
      }
    }

    "the user hasn't logged in" must {

      "return unauthorised" in {

        val authAction = actionToTest("1234567890", new FakeFailingAuthConnector(new MissingBearerToken))
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "the user's session has expired" must {

      "return unauthorised" in {

        val authAction = actionToTest("1234567890", new FakeFailingAuthConnector(new BearerTokenExpired))
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "org user requests a PDF for UTR/URN that matches their enrolment" must {

      "continue with the request" in {

        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", "2647384758")), "Activated", None)
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val action = actionToTest("2647384758", mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "agent user requests a PDF for UTR/URN that matches their enrolment" must {

      "continue with the request" in {
        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-TERSNT-ORG", Seq(EnrolmentIdentifier("URN", "XTTRUST80837546")), "Activated", None)
              .withDelegatedAuthRule("trust-auth")
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val action = actionToTest("XTTRUST80837546", mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "org user requests a PDF for UTR/URN that does not match their enrolment" must {

      "return unauthorised" in {
        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-TERSNT-ORG", Seq(EnrolmentIdentifier("URN", "XTTRUST12746273")), "Activated", None)
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val action = actionToTest("XTTRUST80837546", mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }

    "agent org user requests a PDF for UTR/URN that does not match their enrolment" must {

      "return unauthorised" in {
        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-TERS-ORG", Seq(EnrolmentIdentifier("UTR", "1957385728")), "Activated", None)
              .withDelegatedAuthRule("trust-auth")
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val action = actionToTest("0043748273", mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }

    "user requests a PDF for UTR/URN that is not a valid identifier" must {

      "return internal server error" in {
        val enrolments = Enrolments(
          Set(
            Enrolment("HMRC-TERS-ORG", Seq(EnrolmentIdentifier("UTR", "1957385728")), "Activated", None)
              .withDelegatedAuthRule("trust-auth")
          )
        )

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

        val action = actionToTest("84759873598738597397598", mockAuthConnector)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    Future.failed(exceptionToReturn)
  }
  
}

class FakeAuthConnector(stubbedRetrievalResult: Future[_]) extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    stubbedRetrievalResult.map(_.asInstanceOf[A])
  }

}
