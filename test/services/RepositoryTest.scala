package services

import models.{Assessment, CandidateAnswer, dataDb}
import org.scalatestplus.play.PlaySpec
import repositories.RepoExamSimulator

class RepositoryTest extends PlaySpec{

  val db=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams","assessments")
  val repo = new RepoExamSimulator()
  val dataB=repo.connect(db)

  "RepoExamSimulator.getAllAvailableExams" must {
    "get all available exams by MongoDb exams lst" in {
      val lst = repo.getAllAvailableExams
      lst.length must be >= 2
    }
    "get all available exams by MongoDb questions in exam" in {
      val repo = new RepoExamSimulator()
      repo.connect(db)
      val lst = repo.getAllAvailableExams
      lst.head.listQuestion.length must be > 2
    }
  }

  "RepoExamSimulator.Assessments" must {
    "save/delete assessment" in {
      val exs=new ExamSimulator()
      val exams=exs.exploreExamsFromDb(db)
      val assessment=exs.createAssessment(exams.head.Id,10,"TestCandidate")
      val res = repo.saveAssessment(assessment)
      res mustBe 1
      val resD=repo.deleteAssessment(assessment)
      resD mustBe(1)
    }
    "load assessment" in {
      val exs=new ExamSimulator()
      val exams=exs.exploreExamsFromDb(db)
      val assessment=exs.createAssessment(exams.head.Id,10,"TestCandidate")
      val res = repo.saveAssessment(assessment)
      val lst = repo.getAllAssessment
      lst.length.mustBe(1)
      lst.head.Id.mustBe(assessment.Id)
      repo.deleteAssessment(assessment)
    }
    "update assessment" in {
      val exs=new ExamSimulator()
      val exams=exs.exploreExamsFromDb(db)
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

}
