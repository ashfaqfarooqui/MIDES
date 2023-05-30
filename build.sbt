name := "Model Building"

version := "2.0-alpha"
ThisBuild / scalaVersion := "2.13.10"
                                                                       //

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(midesLib, models, mides)


lazy val midesLib = (project in file("MidesLib")).settings(
  settings,
  deps,
  unmanagedJars in Compile ++= Seq(
    new java.io.File("lib/Supremica.jar"),
    new java.io.File("lib/SupremicaLib.jar"),
     new File("lib/libsumo-1.16.0.jar")
  ).classpath,
  unmanagedJars in Runtime ++= Seq(
    new java.io.File("lib/Supremica.Jar"),
    new java.io.File("lib/SupremicaLib.jar"),
    new File("lib/libsumo-1.16.0.jar")

  ).classpath
)
lazy val mides =
  (project in file("Mides")).settings(settings, deps).dependsOn(midesLib, models)
lazy val models = (project in file("Models")).settings(settings, deps).dependsOn(midesLib)



val deps = libraryDependencies ++= Seq(
  "org.scala-graph"         %% "graph-core"               % "1.13.5",
  "org.scala-graph"         %% "graph-dot"                % "1.13.3",
  "com.github.nscala-time"  %% "nscala-time"              % "2.32.0",
  "org.clapper"             %% "grizzled-slf4j"           % "1.3.4",
  "org.slf4j"                % "slf4j-api"                % "1.7.25",
  "org.apache.logging.log4j" % "log4j-slf4j-impl"         % "2.12.1",
  "org.apache.logging.log4j" % "log4j-api"                % "2.12.1",
  "org.apache.logging.log4j" % "log4j-core"               % "2.12.1",
  "com.github.tototoshi"    %% "scala-csv"                % "1.3.10",
  "com.github.andr83"       %% "scalaconfig"              % "0.7",
  "com.typesafe"             % "config"                   % "1.4.0",
  "com.stephenn"            %% "scala-datatable"          % "0.9.0",
  "org.scalactic"           %% "scalactic"                % "3.2.15",
  "org.scalatest"           %% "scalatest"                % "3.2.15" % "test",
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "2.2.0",
"org.eclipse.sumo" % "libsumo" % "1.17.0",
"org.eclipse.sumo" % "libtraci" % "1.17.0"
)


lazy val settings =
  commonSettings ++
    scalafmtSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Djava.library.path=../sumo/bin"
)
lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
    "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
    "sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "eclipse-sumo" at "https://repo.eclipse.org/content/repositories/sumo-releases/",
    Resolver.mavenLocal
  )
)
