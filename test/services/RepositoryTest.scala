package services

import models.dataDb
import org.scalatestplus.play.PlaySpec
import repositories.{ RepoExamSimulator}

class RepositoryTest extends PlaySpec{

  "RepoExamSimulator.getAllAvailableExams" must {
    "get all available exams by MongoDb exams lst" in {
      val repo = new RepoExamSimulator()
      val db=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams")
      val dataB=repo.connect(db)
      val lst = repo.getAllAvailableExams
      lst.length must be >= 2
    }
    "get all available exams by MongoDb questions in exam" in {
      val repo = new RepoExamSimulator()
      val db=dataDb("localhost:27017","mongoadmin","mongoadmin","examsim","exams")
      repo.connect(db)
      val lst = repo.getAllAvailableExams
      lst.head.listQuestion.length must be > 2
    }
  }


}
