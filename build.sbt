lazy val scalaSettings = Seq(
  scalaVersion := "2.12.2",
  scalacOptions ++= Seq(
    "-deprecation",
    "-Ywarn-value-discard",
    "-Xfatal-warnings"
  )
)

lazy val dockerSettings = Seq(
  // https://github.com/marcuslonnberg/sbt-docker
  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    // val artifactTargetPath = s"/app/${artifact.name}"
    // use fixed artifact path to avoid fails on version change when using custom docker command
    val artifactTargetPath = s"/app/${name.value}.jar"

    new Dockerfile {
      from("fabric8/java-alpine-openjdk8-jdk:1.0.10")
      add(artifact, artifactTargetPath)
      expose(8080)
      entryPoint("java")
      cmd("-jar", artifactTargetPath)
    }
  },
  imageNames in docker := Seq(
    ImageName(s"dgg/${name.value}:latest"),
    ImageName(
      namespace = Some("dgg"),
      repository = name.value,
      tag = Some(version.value)
    )
  )
)

val akkaVersion = "2.5.3"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

lazy val `hello-akka` = (project in file("."))
  .settings(dockerSettings: _*)
  .settings(scalaSettings: _*)
  .settings(
    organization := "es.dgg",
    name := "hello-akka",
    version := "0.1.2",
    mainClass in Compile := Some("es.dgg.app.Boot"),
    libraryDependencies ++= Seq(
      // akka
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.0.9",
      //"de.aktey.akka.visualmailbox" %% "collector" % "1.1.0",
      "ch.qos.logback"             % "logback-classic"                    % "1.2.3",
      "org.codehaus.groovy"        % "groovy"                             % "2.4.12"

    )
  )
  .enablePlugins(DockerPlugin)
