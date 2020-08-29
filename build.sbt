fork in run := true
cancelable in Global := true
organization := "vito-c.github.com"
name := "scala-neovim"
version := "0.2.0"
scalaVersion := "2.13.3"

lazy val akkaVersion = "2.6.8"

// lazy val macros = project.in(file("server"))
// lazy val api = project.in(file("server"))
//   .settings(
//     resolvers += "Rally Health" at "https://dl.bintray.com/rallyhealth/maven",
//     scalaVersion := "2.13.3",
//     name := "server",
//     scalacOptions ++= Seq("-Ymacro-annotations"),
//     libraryDependencies ++= Seq(
//       "com.rallyhealth" %% "weepack-v1" % "1.2.0",
//       "com.rallyhealth" %% "weepickle-v1" % "1.2.0",
//       "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
//       "ch.qos.logback" % "logback-classic" % "1.2.3",
//       "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
//       "org.scalatest" %% "scalatest" % "3.1.0" % Test,
//       "com.lihaoyi" %% "utest" % "0.7.2" % "test",
//       "org.scala-lang" % "scala-reflect" % scalaVersion.value,
//       "io.bullet" %% "macrolizer" % "0.5.0" % "compile-internal",
//       "com.lihaoyi" %% "pprint" % "0.5.6"
//
//     ),
//     testFrameworks += new TestFramework("utest.runner.Framework")
//   )
lazy val server = project.in(file("server"))
  .settings(
    resolvers += "Rally Health" at "https://dl.bintray.com/rallyhealth/maven",
    scalaVersion := "2.13.3",
    name := "server",
    scalacOptions ++= Seq("-Ymacro-annotations"),
    libraryDependencies ++= Seq(
      // "com.github.xuwei-k" %% "msgpack4z-core" % "0.3.8",
      // "com.github.xuwei-k" % "msgpack4z-java07" % "0.2.0",
      "com.rallyhealth" %% "weepack-v1" % "1.2.0",
      "com.rallyhealth" %% "weepickle-v1" % "1.2.0",
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "com.lihaoyi" %% "utest" % "0.7.2" % "test",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "io.bullet" %% "macrolizer" % "0.5.0" % "test",
      "org.scalameta" %% "scalameta" % "4.3.21",
      "io.bullet" %% "macrolizer" % "0.5.0" % "compile-internal",
      "com.lihaoyi" %% "pprint" % "0.5.6"

    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
