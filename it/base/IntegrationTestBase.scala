package base

import com.typesafe.config.ConfigFactory
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import uk.gov.hmrc.mongo.test.MongoSupport

trait IntegrationTestBase extends MongoSupport {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(30, Seconds), interval = Span(15, Millis))

  private lazy val config = Configuration(ConfigFactory.load(System.getProperty("config.resource")))

  def dropTheDatabase(): Unit = {
    mongoDatabase.drop()
  }

  lazy val createApplication: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

}
