name := "hl-crawler2"

version := "1.0"

scalaVersion := "2.12.3"

val akkaVersion = "2.5.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion

libraryDependencies += "org.jsoup" % "jsoup" % "1.10.3"

val okHttpVersion = "3.8.1"
val _okhttp = Seq(
  "com.squareup.okhttp3" % "okhttp" % okHttpVersion,
  "com.squareup.okhttp3" % "okhttp-urlconnection" % okHttpVersion
)

libraryDependencies ++= _okhttp