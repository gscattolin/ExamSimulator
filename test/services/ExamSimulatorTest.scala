package services

import org.scalatestplus.play.PlaySpec

import scala.collection.immutable.Nil

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
      val assessment=examSimulator.createAssessment(awsQ.properties.Id,"Candidate")
      assessment.getCorrectAnswers mustBe (0)
      assessment.Id mustBe > 0
    }
  }
}
