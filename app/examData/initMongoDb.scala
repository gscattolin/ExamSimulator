package examData

import java.io.File

import models.{Exam, dataDb}
import repositories.RepoExamSimulator

object initMongoDb extends App{


  if (args.length<1){
    System.out.println("Missing parameter- Please run with folder where are the exams to import -")
    System.exit(-1)
  }


  val repoExamSimulator= new RepoExamSimulator()
  val db=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams","assessments")
  repoExamSimulator.connect(db)

  System.out.println("Init Completed")



  loadAllExamsInsideFolder("app/examData/")

  def loadAllExamsInsideFolder(folder:String)={
    def extractExtension(filename:String):String=filename.substring(filename.lastIndexOf('.')+1)
    val d=new File(folder)
    if (d.exists && d.isDirectory) {
      val filesInFolder:List[File]=d.listFiles.filter(_.isFile).filter(f => extractExtension(f.toString)=="json").toList
      val availableExams=filesInFolder.map(x=>repoExamSimulator.importExamFromFile2Mongo((x)))
      System.out.println(s"Imported successfully ${availableExams.length} exams")
      availableExams.foreach(x => System.out.println(s" Exam Code Imported = ${x.getOrElse(new Exam()).properties.Code} \n"))
    } else {
      System.out.println(s"Cannot find the folder ${d.getCanonicalPath} . Return empty exam list")
      System.exit(-2)
    }
  }

}
