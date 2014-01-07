name := "pico-play"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "com.picocontainer" % "picocontainer" % "3.0-SNAPSHOT",
  "javax.inject" % "javax.inject" % "1"  
)     

 resolvers ++= Seq(
		Resolver.mavenLocal,
		DefaultMavenRepository
	)


play.Project.playJavaSettings
