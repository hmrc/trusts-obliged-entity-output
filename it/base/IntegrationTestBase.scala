package base

import com.typesafe.config.ConfigFactory
import config.AppConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait IntegrationTestBase extends AsyncFreeSpec with Matchers with BeforeAndAfterEach {

  private val config: Configuration = Configuration(ConfigFactory.load(System.getProperty("config.resource")))
  val appConfig = new AppConfig(config, new ServicesConfig(config))

}
