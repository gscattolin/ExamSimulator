package examData

import java.io.File

import models.{Exam, dataDb}
import repositories.RepoExamSimulator

object InitMongoDb extends App {

  val repoExamSimulator= new RepoExamSimulator()
  initRepoDb()
  val exams=importJsonFiles()

  private def initRepoDb()={
    val d=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams","assessments")
    repoExamSimulator.connect(d)
  }



  private def importJsonFiles():List[Exam]={
    def extractExtension(filename:String):String=filename.substring(filename.lastIndexOf('.')+1)
    val examFolder=new File(new File(new File(".").getAbsolutePath).getCanonicalPath)
    val filesInFolder:List[File]=examFolder.listFiles.filter(_.isFile).filter(f => extractExtension(f.toString)==".json").toList
    val availableExams=filesInFolder.map({x=>repoExamSimulator.loadExamFromFile(x)})
    System.out.println(s"Found and Loaded ${availableExams.length}")
    availableExams
  }



}
