import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private lazy val mongoHmrcVersion = "1.3.0"

  private val bootstrapVersion = "7.19.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % mongoHmrcVersion,
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"  % mongoHmrcVersion,
    "uk.gov.hmrc"               %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"             %% "scalatest"                % "3.2.16",
    "com.typesafe.play"         %% "play-test"                % current,
    "org.scalatestplus.play"    %% "scalatestplus-play"       % "5.1.0",
    "org.scalacheck"            %% "scalacheck"               % "1.17.0",
    "com.vladsch.flexmark"      % "flexmark-all"              % "0.64.8",
    "org.mockito"               % "mockito-core"              % "5.4.0",
    "com.github.tomakehurst"    % "wiremock-standalone"       % "2.27.2"
  ).map(_ % "test, it")

}
