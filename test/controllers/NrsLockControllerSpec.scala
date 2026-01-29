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

package controllers

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsBoolean
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import repositories.NrsLockRepository

import scala.concurrent.Future

class NrsLockControllerSpec extends SpecBase {

  private val nrsLockRepository: NrsLockRepository = mock(classOf[NrsLockRepository])

  override def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(
        bind[NrsLockRepository].toInstance(nrsLockRepository)
      )

  private val identifier: String = "1234567890"

  private val controller: NrsLockController = injector.instanceOf[NrsLockController]

  "NrsLockController" when {
    ".getLockStatus" when {

      "locked" must {
        "return true" in {

          when(nrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(true))

          val result: Future[Result] = controller.getLockStatus(identifier)(FakeRequest())

          status(result) mustBe OK

          contentAsJson(result) mustEqual JsBoolean(true)
        }
      }

      "unlocked" must {
        "return false" in {

          when(nrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

          val result: Future[Result] = controller.getLockStatus(identifier)(FakeRequest())

          status(result) mustBe OK

          contentAsJson(result) mustEqual JsBoolean(false)
        }
      }

      "no lock" must {
        "return false" in {

          when(nrsLockRepository.getLock(any(), any())).thenReturn(Future.successful(false))

          val result: Future[Result] = controller.getLockStatus(identifier)(FakeRequest())

          status(result) mustBe OK

          contentAsJson(result) mustEqual JsBoolean(false)
        }
      }
    }
  }

}
