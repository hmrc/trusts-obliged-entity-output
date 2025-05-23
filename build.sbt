
lazy val IntegrationTest = config("it") extend Test

val appName = "trusts-obliged-entity-output"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;..*Reverse.*;..Routes.;prod.*;testOnlyDoNotUseInAppConf.*;views.html.*;" +
      "uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.16",
    PlayKeys.playDefaultPort         := 9780,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings,
    scalacOptions+= "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .settings(
    inConfig(Test)(testSettings)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories   := Seq(
    baseDirectory.value / "it"
  ),
  parallelExecution            := false,
  fork                         := true
)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)

addCommandAlias("scalastyleAll", "all scalastyle test:scalastyle it:scalastyle")
