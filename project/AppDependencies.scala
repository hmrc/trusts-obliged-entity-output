import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val bootstrapVersion = "5.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-27"      % bootstrapVersion,
    "org.reactivemongo"       %% "play2-reactivemongo"            % "0.20.13-play27",
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                % "3.0.9",
    "com.typesafe.play"       %% "play-test"                % current,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3",
    "org.mockito"             % "mockito-all"               % "1.10.19",
    "com.github.tomakehurst"  % "wiremock-standalone"       % "2.27.2",
    "org.pegdown"             % "pegdown"                   % "1.6.0",
    "org.scalacheck"          %% "scalacheck"               % "1.14.3"
  ).map(_ % Test)
}
