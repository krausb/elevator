name := "example-elevator"

version := "1.0"

scalaVersion := "2.11.8"

description := "Scala Examples - Reactive Elevator Control Center"
homepage := Some(url("https://bitbucket.org/ceth/example-elevator"))

licenses := Seq("GNU General Public License (GPL), Version 3.0"
  -> url("http://opensource.org/licenses/GPL-3.0"))

resolvers ++= Seq(
  "twitter" at "http://maven.twttr.com",
  "sonatype" at "https://oss.sonatype.org/content/groups/public"
)

libraryDependencies ++= {
  val akkaV       = "2.4.7"
  val scalaTestV  = "3.0.0"
  Seq(
    "com.typesafe.akka"           %% "akka-actor"                        % akkaV,
    "com.typesafe.akka"           %% "akka-slf4j"                        % akkaV,
    "com.typesafe.akka"           %% "akka-contrib"			                 % akkaV,
    "com.typesafe.akka"           %% "akka-stream"                       % akkaV,
    "com.typesafe"                %  "config"                            % "1.3.0",
    "com.github.scopt"            %% "scopt"                             % "3.5.0",
    "org.reactivestreams"         %  "reactive-streams"                  % "1.0.0",

    "org.slf4j" 		              %  "slf4j-api" 	                       % "1.7.21",
    "ch.qos.logback"              %  "logback-classic"                   % "1.1.7"  % "compile,runtime,test",
    "org.log4s"                   %% "log4s"                             % "1.3.0",

    "org.scalacheck"              %% "scalacheck"                        % "1.13.1" % "test",
    "com.typesafe.akka"           %% "akka-testkit"                      % akkaV    % "test",
    "org.scalactic"               %% "scalactic"                         % scalaTestV,
    "org.scalatest"               %% "scalatest"                         % scalaTestV  % "test"
  )
}

scalacOptions ++= Seq(
  "-deprecation",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

wartremover.wartremoverSettings

wartremover.wartremoverErrors in (Compile, compile) ++= Seq(
  // Disabled in case of akka problem
  // wartremover.Wart.Any,
  wartremover.Wart.Any2StringAdd,
  wartremover.Wart.EitherProjectionPartial,
  wartremover.Wart.OptionPartial,
  wartremover.Wart.Product,
  wartremover.Wart.Serializable,
  wartremover.Wart.ListOps
)

incOptions := incOptions.value.withNameHashing(true)