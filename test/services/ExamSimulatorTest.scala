package services

import java.io.File
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

import com.typesafe.config.ConfigFactory
import models.{CandidateAnswer, totalAnswers}
import org.scalatestplus.play.PlaySpec
import play.api.{Application, Configuration, Environment, Mode}
import play.api.libs.json.{JsArray, JsPath, JsString, JsSuccess, JsValue, Json, Reads}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ExamSimulatorTest extends PlaySpec{
  val testD="test"+File.separator+"testData"
//  val env = play.api.Environment.simple()
//  val configMock = play.api.Configuration.load(env)
//  val configMock=Configuration("a" -> 1)
//  val application = new GuiceApplicationBuilder()
//    .configure(configMock)
//    .build()
  val myConfigFile = new File("test"+File.separator+"conf"+File.separator+"application.test.conf")
  val parsedConfig = ConfigFactory.parseFile(myConfigFile)
  val configMock = ConfigFactory.load(parsedConfig)


  val examSimulator = new ExamSimulator(Configuration(configMock))
  val sourceType=examSimulator.initService()
  val lst=examSimulator.getAllExams()


  "ExamSimulator.exploreExams" must   {
    "explore find 1 element" in {
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 1
    }

    "aws SAA-C01 element must be included" in {
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 2
      awsA.head.properties.Title  mustEqual  "Amazon AWS Certified Solutions Architect - Associate Exam"
      awsA.head.properties.Version  mustEqual  "1.8"
      awsA.head.listQuestion.length  must be > 1

    }
  }
  "ExamSimulator.getExamInfo" must   {
    "aws SAA-C01 element with 3 question must be there" in {
      lst.length must be  >= 2
      val awsA=lst.filter(x=>x.properties.Code=="SAA-C01")
      awsA.length must be  >= 2
      val awsQ= lst.filter(x=>x.properties.Code=="SAA-C01")
      awsQ.head.listQuestion.length mustBe(3)
      awsQ.head.properties.Code.mustEqual("SAA-C01")
    }
  }

  "ExamSimulator.createAssessment" must   {
    "createAssessment for aws SAA-C01" in {
      val awsQ = lst.filter(x=>x.properties.Code=="SAA-C01").head
      val assessment=examSimulator.createAssessment(awsQ.Id,3,"Candidate")
      assessment.getCorrectAnswers mustBe (0)
      assessment.exam.listQuestion.length mustBe(3)
    }
  }

  "ExamSimulator.getReport" must   {
    "report for aws SAA-C01" in {
      val awsQ = lst.filter(x=>x.properties.Code=="SAA-C01").head
      val assessment=examSimulator.createAssessment(awsQ.Id,3,"Candidate")
      val q=examSimulator.getQuestionInAssessment(assessment.Id,1)
      var rispOk=new ListBuffer[String]
      var rispKo=new ListBuffer[String]
      if (q._2.Text.contains("Amazon Redshift")){rispOk.addOne("B");rispKo.addOne("C")}
      if (q._2.Text.contains("unpredictable traffic")) {rispOk.addOne("A");rispOk.addOne("B");rispKo.addAll(List("A","C"))}
      if (q._2.Text.contains("legacy application")) {rispOk.addAll(List("A","C"));rispKo.addAll(List("D","B"))}
      val correctAnsw=examSimulator.checkUserAnswer(assessment.Id,1,CandidateAnswer(1,rispOk.toList,true))
      val wrongAnsw=examSimulator.checkUserAnswer(assessment.Id,1,CandidateAnswer(1,rispKo.toList,true))
      correctAnsw.mustBe(true)
      wrongAnsw.mustBe(false)
    }
  }

  "ExamSimulator.getInfo" must   {
    "get Time" in {
      val awsQ = lst.filter(x=>x.properties.Code=="SAA-C01").head
      val assessment=examSimulator.createAssessment(awsQ.Id,3,"Candidate")
      val tt=LocalTime .now
      assessment.setUpdateTime(LocalTime.now().plus(100, ChronoUnit.MINUTES))
      val tInSeconds=assessment.getPassedTime()
      tInSeconds.mustBe(100*60)

    }
  }

  "ExamSimulator.json convertor" must {
    "convert map results using basic " in {
      val json: JsValue = Json.parse(
        """
        {
          "1" :  [
            "A","B"
          ],
          "2" :  [
            "B"
          ],
          "3" :  [
            "D","C"
          ]
        }
        """)
      //val json = Json.toJson(Map(1 -> List("A","B"), 2 -> List("B"), 3 -> List("C","D")))
      var i: Int = 1
      var valid: Boolean = true
      var userAnswersRes: mutable.Map[Int, totalAnswers] = mutable.HashMap.empty
      while (valid) {
        if (json.\(i.toString).isEmpty) {
          valid = false
        }
        else {
          val tA = totalAnswers(i, json.\(i.toString).get.as[List[String]])
          userAnswersRes = userAnswersRes + (i -> tA)
          i += 1
        }
      }
      userAnswersRes.size mustBe (3)
    }

    "convert map results using 2 attempt " in {
      val json: JsValue = Json.parse(
        """
        [
          ["1" ,  [
            "A","B"
          ]],
          ["2" ,  [
            "B"
          ]],
          ["3" ,  [
            "D","C"
          ]]
        ]
        """)
      def v2I(jsValue: JsValue):Int={
        Integer.parseInt(jsValue.asInstanceOf[JsString].value)
      }

      def v2Lst(jsValue: JsValue):List[String]={
        val aa=jsValue.asInstanceOf[JsArray].value.map(x=> x.as[String]).toList
        aa
      }
      val userAnswersRes:Map[Int,totalAnswers] = json.as[List[JsArray]].map(x=> v2I(x.value(0))  -> totalAnswers(v2I(x.value(0)),v2Lst(x.value(1)))).toMap
      userAnswersRes.size mustBe (3)
    }
  }



}
