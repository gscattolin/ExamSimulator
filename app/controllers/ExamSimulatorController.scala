package controllers

import javax.inject.{Inject, Singleton}
import models.{CandidateAnswer, CandidateAnswerReport, Exam, PossibleAnswer, Question, totalAnswers}
import play.api.libs.json.{JsArray, JsObject, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}
import play.api.libs.functional.syntax._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.GenExamSimulator



@Singleton
class ExamSimulatorController @Inject() (cc: ControllerComponents,exs: GenExamSimulator) extends AbstractController(cc) {

  val defaultExtension:String="json"
  case class formAnswer(examId:Int,questionsNumber:Int)


  def index = Action {
    val lstExams=exs.exploreExams("app\\examData",defaultExtension)
    Ok(views.html.index("TEST"))
  }


  def getExamList()=Action{

    var folder="app\\examData"
    if (! System.getProperty("os.name").contains("Window")) folder=folder.replace('\\', '/')
    val lstExams=exs.exploreExams(folder,defaultExtension)
    val lstExamsMap:List[Map[String,String]]=lstExams.map(
      x=>Map(
        "id" -> x.properties.Id.toString,
        "code"->x.properties.Code,
        "version" -> x.properties.Version,
        "title" -> x.properties.Title,
        "questions" -> x.listQuestion.length.toString
      )
    )
    Ok(Json.toJson(lstExamsMap))
  }


  def createAssessment()=Action{ request =>
    def reads(json: JsValue): JsResult[formAnswer] = {
      val v1 = (json \ "examId").as[String]
      val v2 = (json \ "questions").as[String]
      JsSuccess(formAnswer(v1.toInt, v2.toInt))
    }
    val json = request.body.asJson.get
    val frmAnsw = json.as[formAnswer](reads)
    val assessment=exs.createAssessment(frmAnsw.examId,frmAnsw.questionsNumber,"CandidateFakeName")
    Ok(Json.toJson(Map("assessmentId"->assessment.Id)))
  }

  def getQuestionByAssessment(assessmentId:Int,questionsId:Int)=Action{req =>
    implicit val possibleAnswerWrites: Writes[PossibleAnswer] = (
      (JsPath \ "placeHolder").write[String] and
        (JsPath \ "Text").write[String]
      )(unlift(PossibleAnswer.unapply))
    implicit val questionWrites: Writes[Question] = (
      (JsPath \ "Id").write[Int] and
        (JsPath \ "Text").write[String] and
        (JsPath \ "Answers").write[Seq[PossibleAnswer]] and
        (JsPath \ "CorrectAnswers").write[Seq[String]] and
        (JsPath \ "Explanation").write[String] and
        (JsPath \ "Valid").write[Boolean]
      )(unlift(Question.unapply))
    val questionAssessmentT=exs.getQuestionInAssessment(assessmentId,questionsId)
    val res=Json.toJson(questionAssessmentT._2)
    Ok(res)
  }

  def checkAnswerByAssessment(assessmentId:Int,questionsId:Int)=Action { request =>
    val json = request.body.asJson.get
    val userAnswersRes=(json \ "answers").as[Seq[String]].toList
    if (userAnswersRes.isEmpty)
      Ok(Json.toJson(Map("isCorrect"->false)))
    val candAnsws=CandidateAnswer(questionsId,userAnswersRes,Correct = false)
    val correct=exs.checkUserAnswers(assessmentId,questionsId,candAnsws)
    Ok(Json.toJson(Map("isCorrect"->correct)))
  }



  def collectAnswers(assessmentId:Int)=Action { request =>
    def convertJson2Answers(jsValue: JsValue):Map[Int,totalAnswers]={
      def v2I(jsValue: JsValue):Int={
        Integer.parseInt(jsValue.asInstanceOf[JsString].value)
      }

      def v2Lst(jsValue: JsValue):List[String]={
        val aa=jsValue.asInstanceOf[JsArray].value.map(x=> x.as[String]).toList
        aa
      }
      val userAnswersRes:Map[Int,totalAnswers] = jsValue.as[List[JsArray]].map(x=> v2I(x.value(0))  -> totalAnswers(v2I(x.value(0)),v2Lst(x.value(1)))).toMap
      userAnswersRes
    }

    val json = request.body.asJson.get
    if ((json \ "answers").isEmpty){
      NotAcceptable("Incorrect Json Format")
    }
    else{
      val userAnswersRes=convertJson2Answers((json \ "answers").get)
      //val userAnswersRes:Map[Int,totalAnswers] = json.as[Map[String, JsValue]].map(x => x._1.toInt -> totalAnswers(x._1.toInt, x._2.as[List[String]]))
      if (userAnswersRes.isEmpty)
        Ok(Json.toJson(Map("verifiedAnswered"->0)))
      userAnswersRes.foreach(x=>exs.checkUserAnswers(assessmentId,x._1,CandidateAnswer(x._1,x._2.Answers,false)))
      Ok(Json.toJson(Map("verifiedAnswered"->userAnswersRes.size)))
    }
  }

  def getAssessmentReportInfo(assessmentId:Int,prop:Int)=Action { request =>
    val timeInSeconds= exs.getAssessmentInfo(assessmentId,prop).toString
    Ok(Json.toJson(Map("timeinseconds"->timeInSeconds)))
  }

  def getAssessmentReport(assessmentId:Int)=Action{ request =>
    implicit val candidateAnswerWrites: Writes[CandidateAnswerReport] = (
        (JsPath \ "Id").write[Int] and
          (JsPath \ "Text").write[String] and
          (JsPath \ "Answers").write[Seq[String]] and
        (JsPath \ "placeHolders").write[Seq[String]] and
          (JsPath \ "Correct").write[Boolean] and
          (JsPath \ "correctPlaceHolders").write[Seq[String]] and
            (JsPath \ "Explanation").write[String]
      )(unlift(CandidateAnswerReport.unapply))
    val t=exs.getReportOnAssessment(assessmentId)
    Ok(Json.toJson(t))
  }

  def getAssessmentQuestions(assessmentId:Int)=Action { request =>
    Ok(Json.toJson(Map("TotalQuestions"->exs.getTotalQuestionaInAssessment(assessmentId))))
  }

}
