package services

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}
import java.util.Calendar

import models.CandidateAnswer
import org.scalatestplus.play.PlaySpec

import scala.collection.immutable.Nil
import scala.collection.mutable.ListBuffer

class ExamSimulatorTest extends PlaySpec{
  val testD="test\\testData"
  "ExamSimulator.exploreExams" must   {
    "explore find 1 element" in {
      val examSimulator = new ExamSimulator()
      val lst = examSimulator.exploreExams(testD, "json")
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 1
    }
    "aws SAA-C01 element must be included" in {
      val examSimulator = new ExamSimulator()
      val lst = examSimulator.exploreExams(testD, "json")
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 2
      awsA.head.properties.Title  mustEqual  "Amazon AWS Certified Solutions Architect - Associate Exam"
      awsA.head.properties.Version  mustEqual  "1.8"
      awsA.head.properties.Id mustEqual(1)
      awsA.head.listQuestion.length  must be > 1

    }
  }
  "ExamSimulator.getExamInfo" must   {
    "aws SAA-C01 element with 3 question must be there" in {
      val examSimulator = new ExamSimulator()
      val lst = examSimulator.exploreExams(testD, "json")
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 2
      val awsQ= lst.filter(x=>x.properties.Code=="SAA-C01"&& x.properties.Filename=="test\\testData\\exam2Test.json")
      awsQ.head.listQuestion.length mustBe(3)
      awsQ.head.properties.Code.mustEqual("SAA-C01")
    }
  }

  "ExamSimulator.createAssessment" must   {
    "createAssessment for aws SAA-C01" in {
      val examSimulator = new ExamSimulator()
      val awsQ = examSimulator.exploreExams(testD, "json").
        filter(x=>x.properties.Code=="SAA-C01"&& x.properties.Filename=="test\\testData\\exam2Test.json").head
      val assessment=examSimulator.createAssessment(awsQ.properties.Id,3,"Candidate")
      assessment.getCorrectAnswers mustBe (0)
      assessment.Id must be > 0
      assessment.exam.listQuestion.length mustBe(3)
    }
  }

  "ExamSimulator.getReport" must   {
    "report for aws SAA-C01" in {
      val examSimulator = new ExamSimulator()
      val awsQ = examSimulator.exploreExams(testD, "json").
        filter(x=>x.properties.Code=="SAA-C01"&& x.properties.Filename=="test\\testData\\exam2Test.json").head
      val assessment=examSimulator.createAssessment(awsQ.properties.Id,3,"Candidate")
      val q=examSimulator.getQuestionInAssessment(assessment.Id,1)
      var rispOk=new ListBuffer[String]
      var rispKo=new ListBuffer[String]
      if (q._2.Text.contains("Amazon Redshift")){rispOk.addOne("B");rispKo.addOne("C")}
      if (q._2.Text.contains("unpredictable traffic")) {rispOk.addOne("A");rispOk.addOne("B");rispKo.addAll(List("A","C"))}
      if (q._2.Text.contains("legacy application")) {rispOk.addAll(List("A","C"));rispKo.addAll(List("D","B"))}
      val correctAnsw=examSimulator.checkUserAnswers(assessment.Id,1,CandidateAnswer(1,rispOk.toList,true))
      val wrongAnsw=examSimulator.checkUserAnswers(assessment.Id,1,CandidateAnswer(1,rispKo.toList,true))
      correctAnsw.mustBe(true)
      wrongAnsw.mustBe(false)
    }
  }

  "ExamSimulator.getInfo" must   {
    "get Time" in {
      val examSimulator = new ExamSimulator()
      val awsQ = examSimulator.exploreExams(testD, "json").
        filter(x=>x.properties.Code=="SAA-C01"&& x.properties.Filename=="test\\testData\\exam2Test.json").head
      val assessment=examSimulator.createAssessment(awsQ.properties.Id,3,"Candidate")
      val tt=LocalTime .now
      assessment.setUpdateTime(LocalTime.now().plus(100, ChronoUnit.MINUTES))
      val tInSeconds=assessment.getPassedTime()
      tInSeconds.mustBe(100*60)

    }
  }


}
