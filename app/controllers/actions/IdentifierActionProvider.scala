/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import play.api.mvc.BodyParsers
import services.AuthenticationService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

class AuthenticatedIdentifierActionProvider @Inject()()(implicit val authConnector: AuthConnector,
                                                        trustAuthService: AuthenticationService,
                                                        val parser: BodyParsers.Default,
                                                        executionContext: ExecutionContext) extends IdentifierActionProvider {

  override def apply(identifier: String): IdentifierAction = new AuthenticatedIdentifierAction(identifier, trustAuthService, authConnector)
}

trait IdentifierActionProvider {
  def apply(identifier: String): IdentifierAction
}