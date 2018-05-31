fork in run := true
cancelable in Global := true
organization := "vito-c.github.com"
name := "scala-neovim"
version := "0.1"
scalaVersion := "2.12.4"
libraryDependencies += "com.github.xuwei-k" %% "msgpack4z-core" % "0.3.8"
libraryDependencies += "com.github.xuwei-k" % "msgpack4z-java07" % "0.2.0"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"
/* libraryDependencies ++= Seq("actor") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.1" } //"2.3.12" } */
/* libraryDependencies ++= Seq("testkit") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.1" % "test" } //"2.3.12" % "test" } */
libraryDependencies ++= Seq("actor") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" } //"2.3.12" }
libraryDependencies ++= Seq("testkit") map { "com.typesafe.akka" %% "akka-%s".format(_) % "2.4.20" % "test" } //"2.3.12" % "test" }
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.12"
libraryDependencies +=  "com.chrisneveu" %% "macrame" % "1.2.3"
mainClass in (Compile,run) := Some("Server")
