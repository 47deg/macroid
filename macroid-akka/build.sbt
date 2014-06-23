name := "macroid-akka-fragments"

description := "Some helpers to attach Akka Actors to Android Fragments"

homepage := Some(url("http://github.com/macroid/macroid-akka-fragments"))

organization := "org.macroid"

version := "2.0.0-M2"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-feature", "-deprecation")

crossScalaVersions := Seq("2.10.4", "2.11.1")

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Android" at (file(System.getenv("ANDROID_SDK_HOME")) / "extras" / "android" / "m2repository").getCanonicalFile.toURI.toString
)

libraryDependencies ++= Seq(
  "org.macroid" %% "macroid" % "2.0.0-M2",
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "19.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3" % "provided"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
