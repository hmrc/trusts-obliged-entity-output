/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.Mockito.mock
import services.AuthenticationService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

class IdentifierActionProviderSpec extends SpecBase {

  private val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])
  private val mockAuthService: AuthenticationService = mock(classOf[AuthenticationService])

  "AuthenticatedIdentifierActionProvider" should {
    "create an AuthenticatedIdentifierAction when apply is invoked" in {
      val defaultParser = injector.instanceOf[play.api.mvc.BodyParsers.Default]
      val provider = new AuthenticatedIdentifierActionProvider()(mockAuthConnector, mockAuthService, defaultParser, ExecutionContext.Implicits.global)

      val action = provider("1234567890")

      action mustBe a[AuthenticatedIdentifierAction]
    }
  }
}
