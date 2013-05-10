import sbt._
import sbt.Keys._

object EventhubBuild extends Build {
	val sprayVersion = "1.1-20130416"
  val eventsourcedVersion = "0.5-SNAPSHOT"
  lazy val eventhub = Project(
    id = "eventhub",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "eventhub-flatmap2013",
      organization := "com.danielwestheide",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.1",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "Eligosource Releases Repo" at
        "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases/",
      resolvers += "Eligosource Snapshots Repo" at
        "http://repo.eligotech.com/nexus/content/repositories/eligosource-snapshots/",
			resolvers += "spray repo" at "http://repo.spray.io",
			resolvers += "spray nightlies" at "http://nightlies.spray.io",
      libraryDependencies ++= Seq(
				"com.typesafe.akka" %% "akka-actor" % "2.1.2",
        "org.eligosource" %% "eventsourced-core" % eventsourcedVersion,
        "org.eligosource" %% "eventsourced-journal-mongodb-reactive" % eventsourcedVersion,
				"io.spray" % "spray-can" % sprayVersion,
				"io.spray" % "spray-routing" % sprayVersion,
				"io.spray" %%  "spray-json" % "1.2.3",
        "org.scalaz" %% "scalaz-core" % "7.0.0",
        "org.reactivemongo" %% "reactivemongo" % "0.8",
				"joda-time" % "joda-time" % "2.1",
				"org.joda" % "joda-convert" % "1.3")
    )
  )
}
