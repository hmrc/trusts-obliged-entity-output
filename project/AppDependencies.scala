import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val mongoHmrcVersion = "2.12.0"
  private val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % mongoHmrcVersion,
    "com.networknt"      % "json-schema-validator"     % "2.0.1" exclude ("com.fasterxml.jackson.core", "jackson-databind"),
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoHmrcVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
