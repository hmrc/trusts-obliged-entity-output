import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-27"  % "3.1.0",
    "uk.gov.hmrc"             %% "simple-reactivemongo"       % "7.30.0-play-27",
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "3.1.0",
    "org.scalatest"           %% "scalatest"                % "3.0.8",
    "com.typesafe.play"       %% "play-test"                % current,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3",
    "org.mockito"             % "mockito-all"               % "1.10.19",
    "com.github.tomakehurst"  % "wiremock-standalone"       % "2.25.1",
    "org.pegdown"             % "pegdown"                   % "1.6.0",
    "org.scalacheck"          %% "scalacheck"               % "1.14.3"
  ).map(_ % Test)
}
