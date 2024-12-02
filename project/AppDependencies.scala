import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private lazy val mongoHmrcVersion = "1.9.0"

  private val bootstrapVersion = "9.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-play-30"         % mongoHmrcVersion,
    "com.github.java-json-tools"  % "json-schema-validator"       % "2.2.14",
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-30"  % mongoHmrcVersion,
    "uk.gov.hmrc"               %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatest"             %% "scalatest"                % "3.2.18",
    "org.scalatestplus.play"    %% "scalatestplus-play"       % "7.0.1",
    "org.playframework"         %% "play-test"                % current,
    "org.scalacheck"            %% "scalacheck"               % "1.18.0",
    "com.vladsch.flexmark"      % "flexmark-all"              % "0.64.8",
    "org.mockito"               % "mockito-core"              % "5.12.0",
    "org.wiremock"              % "wiremock-standalone"       % "3.5.4"
  ).map(_ % "test, it")

}
