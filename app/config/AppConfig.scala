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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  val trustDataUrl: String = servicesConfig.baseUrl("trust-data")

  val trustAuthUrl: String = servicesConfig.baseUrl("trusts-auth")

  val nrsUrl: String = servicesConfig.baseUrl("nrs-trusts")

  val nrsToken: String = config.get[String]("microservice.services.nrs-trusts.token")

  val trustDataEnvironment: String = config.get[String]("microservice.services.trust-data.environment")
  val trustDataToken: String = config.get[String]("microservice.services.trust-data.token")
  /**
   * Content-Disposition is 'inline' by default. Change to 'attachment' to download the file with no preview
   */
  val inlineOrAttachment: String = config.get[String]("inline-or-attachment")

  val lockTtlInSeconds: Int = config.get[Int]("mongodb.lock.ttlSeconds")
}
