package controllers

import javax.inject.{Inject, Singleton}
import models.{CandidateAnswer, CandidateAnswerReport, Exam, PossibleAnswer, Question}
import play.api.libs.json.{JsPath, JsResult, JsSuccess, JsValue, Json, Writes}
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
    Ok(Json.toJson(exs.getReportOnAssessment(assessmentId)))
  }

  def getAssessmentQuestions(assessmentId:Int)=Action { request =>
    Ok(Json.toJson(Map("TotalQuestions"->exs.getTotalQuestionaInAssessment(assessmentId))))
  }

}
