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

package base

import config.AppConfig
import controllers.actions.{FakeIdentifierActionProvider, IdentifierActionProvider}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfter, Inside}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.test.Helpers
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.http.HeaderCarrier

class SpecBase extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with BeforeAndAfter
  with GuiceOneServerPerSuite
  with Inside {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val application: Application = applicationBuilder().build()

  def injector: Injector = application.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        Seq(
          "metrics.enabled" -> false,
          "auditing.enabled" -> false
        ): _*
      ).overrides(
      bind[IdentifierActionProvider].toInstance(new FakeIdentifierActionProvider(Helpers.stubControllerComponents().parsers.default, Organisation))
    )
  }
}
