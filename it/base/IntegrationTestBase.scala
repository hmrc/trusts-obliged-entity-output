package base

import controllers.actions.{FakeIdentifierAction, FakeIdentifierActionProvider, IdentifierAction, IdentifierActionProvider}
import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents
import play.api.{Application, Play}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.auth.core.AffinityGroup.Agent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait IntegrationTestBase extends ScalaFutures {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(30, Seconds), interval = Span(15, Millis))

  private val connectionString: String = "mongodb://localhost:27017/trusts-obliged-entity-output-integration"

  private def dropTheDatabase(application: Application): Future[Boolean] = {
    val mongoDriver = application.injector.instanceOf[ReactiveMongoApi]
    mongoDriver.database.map(_.collection[JSONCollection]("nrs-lock")).flatMap { db =>
      db.drop(failIfNotFound = false)
    }
  }

  private val cc: ControllerComponents = stubControllerComponents()

  def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(Seq(
        "mongodb.uri" -> connectionString,
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "mongo-async-driver.akka.log-dead-letters" -> 0
      ): _*)

  def createApplication : Application = applicationBuilder
    .overrides(
      bind[IdentifierActionProvider].toInstance(new FakeIdentifierActionProvider(cc.parsers.default, Agent)),
      bind[IdentifierAction].toInstance(new FakeIdentifierAction(cc.parsers.default, Agent))
    ).build()

  def assertMongoTest(application: Application)(block: Application => Assertion): Future[Assertion] = {

    Play.start(application)

    try {
      val f: Future[Assertion] = dropTheDatabase(application).map(_ => block(application))

      // We need to force the assertion to resolve here.
      // Otherwise, the test block may never be run at all.
      val assertion = Await.result(f, Duration.Inf)
      Future.successful(assertion)
    }
    finally {
      Play.stop(application)
    }
  }
}
