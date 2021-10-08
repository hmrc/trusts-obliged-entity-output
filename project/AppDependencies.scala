import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val bootstrapVersion = "5.9.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "org.reactivemongo" %% "play2-reactivemongo" % "0.20.13-play28",
    ws
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.scalatest" %% "scalatest" % "3.2.9",
    "com.typesafe.play" %% "play-test" % current,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.scalacheck" %% "scalacheck" % "1.15.4",
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10",
    "org.mockito" % "mockito-core" % "3.12.4",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.27.2",
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.scalacheck" %% "scalacheck" % "1.14.3"
  ).map(_ % Test)

  private val akkaVersion = "2.6.12"
  private val akkaHttpVersion = "10.2.3"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec" % "commons-codec" % "1.12"
  )
}