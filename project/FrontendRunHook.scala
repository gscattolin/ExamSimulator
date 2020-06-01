class FrontendRunHook {

}
import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/**
 * Frontend build play run hook.
 * https://www.playframework.com/documentation/2.8.x/SBTCookbook
 */
object FrontendRunHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {

      var process: Option[Process] = None

      /**
       * Change the commands in `FrontendCommands.scala` if you want to use Yarn.
       */
      var install: String = FrontendCmd.dependencyInstall
      var run: String = FrontendCmd.serve

      // Windows requires npm commands prefixed with cmd /c
      if (System.getProperty("os.name").toLowerCase().contains("win")){
        install = "cmd /c" + install
        run = "cmd /c" + run
      }

      /**
       * Executed before play run start.
       * Run npm install if node modules are not installed.
       */
      override def beforeStarted(): Unit = {
        if (!(base / "gui" / "node_modules").exists()) Process(install, base / "gui").!
      }

      /**
       * Executed after play run start.
       * Run npm start
       */
      override def afterStarted(): Unit = {
        process = Some(
          Process(run, base / "gui").run
        )
      }

      /**
       * Executed after play run stop.
       * Cleanup frontend execution processes.
       */
      override def afterStopped(): Unit = {
        process.foreach(p => p.destroy())
        process = None
      }

    }

    UIBuildHook
  }
}