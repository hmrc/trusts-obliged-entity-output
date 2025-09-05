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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustAuthConnector
import models.{TrustAuthAllowed, TrustAuthDenied}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationServiceImpl @Inject()(trustAuthConnector: TrustAuthConnector) (implicit ec: ExecutionContext) extends AuthenticationService with Logging {

  override def authenticateForIdentifier[A](identifier: String)
                                           (implicit request: Request[A],
                                     hc: HeaderCarrier): Future[Either[Result, Request[A]]] =
  {
    trustAuthConnector.authorisedForIdentifier(identifier).flatMap {
      case _: TrustAuthAllowed =>
        Future.successful(Right(request))
      case TrustAuthDenied(_) =>
        Future.successful(Left(Unauthorized))
      case _ =>
        logger.warn(s"[Session ID: ${Session.id(hc)}][UTR/URN: $identifier] Problem authenticating for trust identifier")
        Future.successful(Left(InternalServerError))
    }
  }

}

@ImplementedBy(classOf[AuthenticationServiceImpl])
trait AuthenticationService {

  def authenticateForIdentifier[A](identifier: String)
                                  (implicit request: Request[A],
                                   hc: HeaderCarrier): Future[Either[Result, Request[A]]]
}
