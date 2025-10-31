import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val mongoHmrcVersion = "2.10.0"
  private val bootstrapVersion = "10.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-play-30"         % mongoHmrcVersion,
    "com.github.java-json-tools"  % "json-schema-validator"       % "2.2.14",
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-30"  % mongoHmrcVersion,
    "uk.gov.hmrc"               %% "bootstrap-test-play-30"   % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
