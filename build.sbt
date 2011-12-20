name := "CbiShell"

version := "2.1"

scalaVersion := "2.9.1"

seq(webSettings :_*)

// for Jrebel
jettyScanDirs := Nil

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

// for eclipse
resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.4.0")

libraryDependencies ++= {
    val liftVersion = "2.4-M4"
    Seq(
        "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-widgets" % liftVersion,
        "net.liftweb" %% "lift-testkit" % liftVersion % "compile->default"
    )
}

libraryDependencies ++= Seq(
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty,test",
    "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test",
    "junit" % "junit" % "4.8" % "test->default",
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
    "ch.qos.logback" % "logback-classic" % "0.9.28" % "compile->default",
    "com.h2database" % "h2" % "1.3.157",
    "mysql" % "mysql-connector-java" % "5.1.17",
    "net.databinder" %% "dispatch-http" % "0.8.5"
)


//defaultExcludes ~= (filter => filter || "*~")

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"
