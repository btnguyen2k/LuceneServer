import sbt._
import Keys._
import play.Project._
import com.typesafe.config._
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

object ApplicationBuild extends Build {
    override def settings = super.settings ++ Seq(
        EclipseKeys.skipParents in ThisBuild := false
    )
    
    val conf            = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
    val appName         = conf.getString("app.name").toLowerCase().replaceAll("\\W+", "-")
    val appVersion      = conf.getString("app.version")
    
    val _springVersion = "4.2.0.RELEASE"
    val _luceneVersion = "5.2.1"

    val appDependencies = Seq(
        "org.slf4j"                  %  "log4j-over-slf4j"        % "1.7.12",
        "org.apache.thrift"          %  "libthrift"               % "0.9.2",
        "com.google.guava"           %  "guava"                   % "18.0",

        "org.springframework"        %  "spring-beans"            % _springVersion,
        "org.springframework"        %  "spring-expression"       % _springVersion,
        "org.apache.commons"         %  "commons-dbcp2"           % "2.1.1",
        
        "com.github.ddth"            %  "ddth-queue"              % "0.3.1",
        "com.github.ddth"            %  "cassdir"                 % "0.1.1",
        "com.github.ddth"            %  "redir"                   % "0.1.1",
        "org.apache.lucene"          %  "lucene-core"             % _luceneVersion,
        "org.apache.lucene"          %  "lucene-analyzers-common" % _luceneVersion,
        "org.apache.lucene"          %  "lucene-queries"          % _luceneVersion,
        "org.apache.lucene"          %  "lucene-queryparser"      % _luceneVersion,
        
        "com.github.ddth"            %  "ddth-thriftpool"         % "0.2.1.3" % "test",
        "org.jodd"                   %  "jodd-http"               % "3.6.6" % "test",
        
        "com.github.ddth"            %% "play-module-plommon"     % "0.5.1.5",

        filters
    )
    
    var _javaVersion = "1.7"
    
    val main = play.Project(appName, appVersion, appDependencies).settings(
        // Disable generating scaladoc
        sources in doc in Compile := List(),
        
        // Custom Maven repositories
        resolvers += "Sonatype OSS repository" at "https://oss.sonatype.org/content/repositories/releases/",
        
        //resolvers += "orestes-bloom-filter" at "https://raw.githubusercontent.com/Baqend/Orestes-Bloomfilter/master/maven-repo",
        
        // Force compilation in targetted java version
        javacOptions in Compile ++= Seq("-source", _javaVersion, "-target", _javaVersion)
    )
}
