logLevel := Level.Warn

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "twitter" at "http://maven.twttr.com",
  "sonatype" at "https://oss.sonatype.org/content/groups/public"
)

addSbtPlugin("org.scoverage"            % "sbt-scoverage"           % "1.4.0")
addSbtPlugin("com.orrsella"             % "sbt-stats"               % "1.0.5")
addSbtPlugin("com.typesafe.sbt"         % "sbt-git"                 % "0.8.5")
addSbtPlugin("org.scalariform"          % "sbt-scalariform"         % "1.6.0")
addSbtPlugin("com.typesafe.sbteclipse"  % "sbteclipse-plugin"       % "4.0.0")
addSbtPlugin("com.eed3si9n"             % "sbt-assembly"            % "0.14.1")
addSbtPlugin("org.typelevel"            % "sbt-typelevel"           % "0.3.1")
addSbtPlugin("org.scalastyle"           %% "scalastyle-sbt-plugin"  % "0.8.0")
addSbtPlugin("org.wartremover"          % "sbt-wartremover"         % "1.1.1")
addSbtPlugin("net.virtual-void"         % "sbt-dependency-graph"    % "0.8.1")
addSbtPlugin("com.github.mpeltonen"     % "sbt-idea"                % "1.6.0")
addSbtPlugin("com.typesafe.sbt"         % "sbt-license-report"      % "1.2.0")
addSbtPlugin("com.github.gseitz"        % "sbt-release"             % "1.0.3")