name := "money-observer"
 
version := "1.0" 
      
lazy val `money-observer` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Atlassian" at "https://maven.atlassian.com/content/repositories/atlassian-public/"

scalaVersion := "2.12.2"

val silhouetteVersion = "5.0.3"
val reactiveMongoVersion = "0.12.7-play26"
val playJsonVersion = "2.6.8"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  specs2 % Test,
  guice,
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  "com.typesafe.play" %% "play-json" % playJsonVersion,
  "com.typesafe.play" %% "play-json-joda" % playJsonVersion,
  "com.iheart" %% "ficus" % "1.4.3",
  "net.codingwell" %% "scala-guice" % "4.1.1",
  "org.mockito" % "mockito-core" % "2.13.0" % Test
)

routesGenerator := InjectedRoutesGenerator