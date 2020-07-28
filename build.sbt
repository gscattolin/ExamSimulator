
lazy val ExamSim = (project in file(".")).enablePlugins(PlayScala)
  .settings(
    name := "ExamSimulator",
    maintainer := "Giancarlo Scattolin",
    scalaVersion := "2.13.2",
      libraryDependencies += guice,
    javaHome := Some(file("/usr/")),
    libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
    libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.20.11-play27",
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    libraryDependencies +="com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4",
      version := "1.0"
  )



      