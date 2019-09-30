name := "Model Building"

version := "1.0"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  "org.scala-graph" %% "graph-core" % "1.12.1",
  "org.scala-graph" %% "graph-dot" % "1.12.1",
  "org.eclipse.milo" % "sdk-client" % "0.3.3",
  "org.eclipse.milo" % "stack-client" % "0.3.3",
  "com.github.nscala-time" %% "nscala-time" % "2.18.0",
  //"ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.clapper" %% "grizzled-slf4j" % "1.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.github.martincooper" %% "scala-datatable" % "0.8.0",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)
