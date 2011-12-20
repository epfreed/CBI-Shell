resolvers ++= Seq(
        "Web plugin repo" at "http://siasia.github.com/maven2",
        Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

addSbtPlugin("com.github.siasia" % "xsbt-web-plugin" % "0.1.2")


//for eclipse

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.4.0")
