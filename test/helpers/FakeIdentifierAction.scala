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

package helpers

import controllers.actions.{IdentifierAction, IdentifierActionProvider}
import models.UTR
import models.requests.IdentifierRequest
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import play.api.test.Helpers
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifierActionProvider @Inject()(bodyParsers: BodyParser[AnyContent], affinityGroup: AffinityGroup) extends IdentifierActionProvider {
  override def apply(identifier: String): IdentifierAction = new FakeIdentifierAction(bodyParsers, affinityGroup)
}


class FakeIdentifierAction @Inject()(bodyParsers: BodyParser[AnyContent], affinityGroup: AffinityGroup) extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    block(IdentifierRequest(request, "InternalId", UTR("1234567890"), "SessionID", affinityGroup))
  }
  override def parser: BodyParser[AnyContent] =
    bodyParsers

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
