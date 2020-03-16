name := "Model Building"

version := "1.0"

scalaVersion := "2.12.10"


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


//if (true) {
unmanagedJars in Compile ++= Seq(
  new java.io.File("/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar")
).classpath
unmanagedJars in Runtime ++= Seq(
  new java.io.File("/usr/local/MATLAB/R2019b/extern/engines/java/jar/engine.jar")
).classpath
//}

libraryDependencies ++= Seq(
  "org.scala-graph"        %% "graph-core"  % "1.12.1",
  "org.scala-graph"        %% "graph-dot"   % "1.12.1",
  "org.eclipse.milo"       % "sdk-client"   % "0.3.3",
  "org.eclipse.milo"       % "stack-client" % "0.3.3",
  "com.github.nscala-time" %% "nscala-time" % "2.18.0",
  "org.clapper" %% "grizzled-slf4j" % "1.3.4",
  "org.slf4j"   % "slf4j-api"       % "1.7.25",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.12.1",
  "org.apache.logging.log4j" % "log4j-api"        % "2.12.1",
  "org.apache.logging.log4j" % "log4j-core"       % "2.12.1",
  "com.github.tototoshi"     %% "scala-csv"       % "1.3.5",
  "com.github.andr83"        %% "scalaconfig"     % "0.4",
  "com.typesafe"             % "config"           % "1.4.0",
  "com.github.martincooper"  %% "scala-datatable" % "0.8.0",
  "org.scalactic"            %% "scalactic"       % "3.0.5",
  "org.scalatest"            %% "scalatest"       % "3.0.5" % "test",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

  // "nl.vroste"               %% "scala-ads-client" % "0.1.0-SNAPSHOT"
)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)
