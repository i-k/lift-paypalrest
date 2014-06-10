name := "Lift PayPalRest"

version := "0.0.1"

organization := "liftpaypal"

organizationName := "Bittinosturi"

homepage := Some(url("https://github.com/i-k/lift-paypalrest"))

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "staging" at "http://oss.sonatype.org/content/repositories/staging",
  "releases" at "http://oss.sonatype.org/content/repositories/releases"
)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.6-SNAPSHOT"
  val specsVersion = "2.4-SNAPSHOT"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.specs2" %% "specs2-core" % specsVersion % "test",
    "org.specs2" %% "specs2-junit" % specsVersion % "test",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "net.databinder.dispatch" %% "dispatch-lift-json" % "0.11.0"
  )
}
