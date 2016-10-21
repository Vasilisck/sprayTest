name := "spray_test"

version := "1.0"
scalaVersion := "2.11.8"

val sprayVersion = "1.3.3"
val akkaVersion = "2.3.9"
val reactiveMongoVersion = "0.12.0"
val json4sVersion = "3.4.1"
val logBackClassicVersion = "1.7.21"
val spec2Version = "2.3.13"

libraryDependencies ++= Seq(
  "io.spray"            %% "spray-can"            % sprayVersion,
  "io.spray"            %% "spray-routing"        % sprayVersion,
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-slf4j"           % akkaVersion,
  "org.reactivemongo"   %% "reactivemongo"        % reactiveMongoVersion,
  "org.json4s"          %% "json4s-native"        % json4sVersion,
  "org.json4s"          %% "json4s-jackson"       % json4sVersion,
  "org.slf4j"           %  "slf4j-log4j12"        % logBackClassicVersion,
  "io.spray"            %% "spray-testkit"        % sprayVersion  % "test",
  "com.typesafe.akka"   %% "akka-testkit"         % akkaVersion   % "test",
  "org.specs2"          %% "specs2-core"          % spec2Version  % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
    