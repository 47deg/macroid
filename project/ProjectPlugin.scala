import microsites.MicrositesPlugin.autoImport._
import sbt.Keys._
import sbt._

object ProjectPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val micrositeSettings = Seq(
      micrositeName := "macroid",
      micrositeDescription := "A modular functional user interface creation language for Android," +
        "" + " implemented with Scala macros",
      micrositeBaseUrl := "macroid",
      micrositeDocumentationUrl := "/macroid/docs/",
      micrositeGithubOwner := "47deg",
      micrositeGithubRepo := "macroid",
      micrositePalette := Map(
        "brand-primary" -> "#F24130",
        "brand-secondary" -> "#203040",
        "brand-tertiary" -> "#1B2A38",
        "gray-dark" -> "#4E4E4E",
        "gray" -> "#7C7C7C",
        "gray-light" -> "#E9E9E9",
        "gray-lighter" -> "#F7F7F7",
        "white-color" -> "#FFFFFF"
      )
    )
    val androidV = "25.0.1"
    val platformV = "android-25"

    val commonSettings =Seq(
      version := "3.0.0-SNAPSHOT",
      licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
      scalaVersion := "2.11.11",
      crossScalaVersions := Seq("2.10.6", "2.11.11"),
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
      scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-target:jvm-1.8",
        "-encoding",
        "UTF-8",
        "-unchecked",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code") ++ (scalaBinaryVersion.value match {
        case "2.10" => Seq.empty
        case v      => Seq("-Ywarn-unused-import")
      }),
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.0.1" % Test,
        "com.android.support" % "support-v4" % androidV),
      parallelExecution in Test := false,
      fork in Test := true,
      organization := "org.47deg",
      organizationName := "47 Degrees",
      organizationHomepage := Some(new URL("http://47deg.github.io/macroid")),
      publishMavenStyle := true,
      startYear := Some(2015),
      description := "A Scala GUI DSL for Android",
      homepage := Some(url("http://47deg.github.io/macroid")),
      scmInfo := Some(
        ScmInfo(url("https://github.com/47deg/macroid"),
                "https://github.com/47deg/macroid.git")),
      pomExtra := <developers>
        <developer>
          <name>macroid</name>
        </developer>
      </developers>,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      credentials += Credentials("Sonatype Nexus Repository Manager",
                                 "oss.sonatype.org",
                                 sys.env.getOrElse("PUBLISH_USERNAME", ""),
                                 sys.env.getOrElse("PUBLISH_PASSWORD", "")),
      publishArtifact in Test := false
    ) ++ addCommandAlias(
      "testAndCover",
      "; clean; coverage; test; coverageReport; coverageAggregate")

    lazy val macroAnnotationSettings = Seq(
      addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full),
      scalacOptions += "-Xplugin-require:macroparadise",
      scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
    )

  }

}
