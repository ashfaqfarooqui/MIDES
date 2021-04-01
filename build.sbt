name := "Model Building"

version := "1.0"
ThisBuild / scalaVersion := "2.12.12"
//scalaVersion := "2.12.12"

// javacOptions in (Compile, compile) ++= Seq(                              //
//   "-source",                                                             //
//   "1.8",                                                                 //
//   "-target",                                                             //
//   "1.8",                                                                 //
//   "-Xlint",                                                              //
//   "-classpath",                                                          //
//   "/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar"          //
// )                                                                        //
// scalacOptions in (Compile, compile) ++= Seq(                             //
//   "-target:jvm-1.8",                                                     //
//   "-classpath",                                                          //
//   "/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar"          //
// )                                                                        //
// initialize := {                                                          //
//   val _        = initialize.value // run the previous initialization     //
//   val required = "1.8"                                                   //
//   val current  = sys.props("java.specification.version")                 //
//   assert(                                                                //
//     current == required,                                                 //
//     s"Unsupported JDK: java.specification.version $current != $required" //
//   )                                                                      //
// }                                                                        //

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(midesLib, models, mides, MatlabClient, lsmZ)

val withMatlab = true
val withLSM    = true
lazy val midesLib = (project in file("MidesLib")).settings(
  settings,
  deps,
  unmanagedJars in Compile ++= Seq(
    new java.io.File("lib/Supremica.jar"),
    new java.io.File("lib/SupremicaLib.jar")
  ).classpath,
  unmanagedJars in Runtime ++= Seq(
    new java.io.File("lib/Supremica.Jar"),
    new java.io.File("lib/SupremicaLib.jar")
  ).classpath
)
lazy val mides =
  (project in file("Mides")).settings(settings, deps).dependsOn(midesLib, models, lsmZ)
lazy val models = (project in file("Models")).settings(settings, deps).dependsOn(midesLib)
lazy val MatlabClient = (project in file("MatlabClient"))
  .settings(
    unmanagedJars in Compile ++= Seq(
      new java.io.File("/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar")
    ).classpath,
    unmanagedJars in Runtime ++= Seq(
      new java.io.File("/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar")
    ).classpath
  )
  .settings(deps, settings)
  .dependsOn(midesLib)

lazy val lsmZ = (project in file("LSM-Z"))
  .settings(deps, settings)
  .dependsOn(midesLib, MatlabClient)

//if (withLSM) global.aggregate(lsmZ)
//if (withMatlab) global.aggregate(MatlabClient)

val deps = libraryDependencies ++= Seq(
  "org.scala-graph"         %% "graph-core"               % "1.12.1",
  "org.scala-graph"         %% "graph-dot"                % "1.12.1",
  "org.eclipse.milo"         % "sdk-client"               % "0.3.3",
  "org.eclipse.milo"         % "stack-client"             % "0.3.3",
  "com.github.nscala-time"  %% "nscala-time"              % "2.18.0",
  "org.clapper"             %% "grizzled-slf4j"           % "1.3.3",
  "org.slf4j"                % "slf4j-api"                % "1.7.25",
  "org.apache.logging.log4j" % "log4j-slf4j-impl"         % "2.12.1",
  "org.apache.logging.log4j" % "log4j-api"                % "2.12.1",
  "org.apache.logging.log4j" % "log4j-core"               % "2.12.1",
  "com.github.tototoshi"    %% "scala-csv"                % "1.3.5",
  "com.github.andr83"       %% "scalaconfig"              % "0.4",
  "com.typesafe"             % "config"                   % "1.4.0",
  "com.github.martincooper" %% "scala-datatable"          % "0.8.0",
  "org.scalactic"           %% "scalactic"                % "3.0.5",
  "org.scalatest"           %% "scalatest"                % "3.0.5" % "test",
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.1.2"
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
  "utf8"
)
lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    Resolver.mavenLocal
  )
)
