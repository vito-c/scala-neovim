fork in run := true
cancelable in Global := true
organization := "vito-c.github.com"
name := "scala-neovim"
version := "0.2.0"
scalaVersion := "2.13.3"
/* libraryDependencies ++= Seq("actor") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.1" } //"2.3.12" } */
/* libraryDependencies ++= Seq("testkit") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.1" % "test" } //"2.3.12" % "test" } */

// libraryDependencies += "com.github.xuwei-k" %% "msgpack4z-core" % "0.3.8"
// libraryDependencies += "com.github.xuwei-k" % "msgpack4z-java07" % "0.2.0"
// libraryDependencies += "com.typesafe" % "config" % "1.3.0"
// libraryDependencies ++= Seq("actor") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" } //"2.3.12" }
// libraryDependencies ++= Seq("testkit") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" % "test" } //"2.3.12" % "test" }
// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
// libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.12"
// libraryDependencies +=  "com.chrisneveu" %% "macrame" % "1.2.3"


lazy val akkaVersion = "2.6.8"

lazy val server = project.in(file("server"))
  .settings(
    resolvers += "Rally Health" at "https://dl.bintray.com/rallyhealth/maven",
    // scalaVersion := "2.13.3",
    name := "server",
    libraryDependencies ++= Seq(
      "com.github.xuwei-k" %% "msgpack4z-core" % "0.3.8",
      "com.github.xuwei-k" % "msgpack4z-java07" % "0.2.0",
      // "com.typesafe" % "config" % "1.3.0",
      "com.rallyhealth" %% "weepack-v1" % "1.2.0",
      "com.rallyhealth" %% "weepickle-v1" % "1.2.0",
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  /* .dependsOn(service % "compile;test->test") */
  /* .dependsOn(model % "compile;test->test") */
// mainClass in (Compile,run) := Some("Server")
// libraryDependencies += "com.github.xuwei-k" %% "msgpack4z-core" % "0.3.8",
// libraryDependencies += "com.github.xuwei-k" % "msgpack4z-java07" % "0.2.0",
// libraryDependencies += "com.typesafe" % "config" % "1.3.0",
// libraryDependencies ++= Seq("actor") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" }, //"2.3.12" },
// libraryDependencies ++= Seq("testkit") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" % "test" }, //"2.3.12" % "test" }
// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
// libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.12",
// libraryDependencies +=  "com.chrisneveu" %% "macrame" % "1.2.3",
