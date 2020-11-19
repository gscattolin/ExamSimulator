package services

import java.io.File

import com.typesafe.config.ConfigFactory
import models.{Assessment, CandidateAnswer, Exam, dataDb}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import repositories.RepoExamSimulator

class RepositoryTest extends PlaySpec{
  val db=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams","assessments")
  val repo = new RepoExamSimulator()
  val dataB=repo.connect(db)
  val dbConf=Map("host" ->"localhost:27017","username" ->"mongoadmin","password" -> "mongoadmin","dbName" -> "examsim","MainTable" -> "exams", "AssessmentTable" -> "assessments")
  val dbSources=Map("db" -> dbConf, "files" -> "disabled")
//  lazy val testConfiguration: Configuration =
//    Configuration("datasources" -> dbSources)
  val testConfiguration: Configuration= Configuration(ConfigFactory.load("conf/application.test.conf"))
  val exs=new ExamSimulator(testConfiguration)

  "RepoExamSimulator.getAllAvailableExams" must {
    "get all available exams by MongoDb exams lst" in {
      val lst = repo.getAllAvailableExamsFromDb
      lst.length must be >= 2
    }
    "get all available exams by MongoDb questions in exam" in {
      val repo = new RepoExamSimulator()
      repo.connect(db)
      val lst = repo.getAllAvailableExamsFromDb
      lst.head.listQuestion.length must be > 2
    }
  }

  "RepoExamSimulator.Assessments" must {
    "save/delete assessment" in {
      val exams=exs.getAllExams()
      val assessment=exs.createAssessment(exams.head.Id,10,"TestCandidate")
      val res = repo.saveAssessment(assessment)
      res mustBe 1
      val resD=repo.deleteAssessment(assessment)
      resD mustBe(1)
    }
    "load assessment" in {
      val exams=exs.getAllExams()
      val assessment=exs.createAssessment(exams.head.Id,10,"TestCandidate")
      val res = repo.saveAssessment(assessment)
      val lst = repo.getAllAssessment
      lst.length.mustBe(1)
      lst.head.Id.mustBe(assessment.Id)
      repo.deleteAssessment(assessment)
    }
    "update assessment" in {
      val exams=exs.getAllExams()
      val assessment:Assessment=exs.createAssessment(exams.head.Id,10,"TestCandidate")
      val res = repo.saveAssessment(assessment)
      assessment.candidateAnswers.addOne(CandidateAnswer(1,List("A","B"),true))
      val resUpdate=repo.updateAssessment(assessment)
      val savedA =repo.loadAssessment(assessment.Id).getOrElse(new Assessment())
      savedA.candidateAnswers.head.placeHolders.length.mustBe(2)
      resUpdate mustBe 1
      repo.deleteAssessment(assessment)
    }
  }

  "RepoExamSimulator.Import" must {
    "import exam" in {
      val file2Import=new File("test/testData/exam3Test.json")
      val fileX:Exam=repo.importExamFromFile2Mongo(file2Import).getOrElse(new Exam())
      val newEx:Exam = repo.loadSingleExamByCode(fileX.properties.Code)
      repo.deleteExam(newEx)
      newEx.properties.Code mustBe("SAA-CX")
      newEx.properties.Title mustBe("Test Importing")
      newEx.properties.TimeLimit.mustBe(125)
      newEx.listQuestion.length mustBe(3)
    }
  }

  "RepoExamSimulator.loadExamFromFile" must {
    "removeDuplicates in Load Exams from file" in {
      val file2Import=new File("test/testData/exam4TestDuplQ.json")
      val examX=repo.loadExamFromFile(file2Import)
      val ex=examX.getOrElse(new Exam())
      ex.listQuestion.length mustBe 1
    }
    "corrupted file json exception" in {
      val file2Import=new File("test/testData/examTestCorrupted1.json")
      val examX=repo.loadExamFromFile(file2Import)
      examX.isLeft mustBe true
      examX match {
        case Right(value)=> ;
        case Left(value) => value.Message.nonEmpty mustBe true ; value.Id must be > 0
      }
    }
    "corrupted file json parsing exception" in {
      val file2Import=new File("test/testData/examTestCorrupted2.json")
      val examX=repo.loadExamFromFile(file2Import)
      examX.isLeft mustBe true
      examX match {
        case Right(value)=> ;
        case Left(value) => {
          value.Message.nonEmpty mustBe true
          value.Message.contains("Unexpected") mustBe true
          value.Id must be > 0
        }
      }
    }
  }

}
