resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"          % "sbt-auto-build"         % "3.9.0")
addSbtPlugin("uk.gov.hmrc"          % "sbt-distributables"     % "2.2.0")
addSbtPlugin("com.typesafe.play"    % "sbt-plugin"             % "2.8.20")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"          % "2.0.8")
addSbtPlugin("com.beautiful-scala"  % "sbt-scalastyle"         % "1.5.1")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"            % "0.6.3")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
