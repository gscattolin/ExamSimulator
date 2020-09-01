package controllers

import java.io.File
import java.util.UUID

import javax.inject.{Inject, Singleton}
import models.{CandidateAnswer, CandidateAnswerReport, Exam, PossibleAnswer, Question, totalAnswers}
import play.api.Logger
import play.api.libs.json.{JsArray, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, Writes}
import play.api.libs.functional.syntax._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.GenExamSimulator



@Singleton
class ExamSimulatorController @Inject() (cc: ControllerComponents,exs: GenExamSimulator) extends AbstractController(cc) {

  protected val LOGGER: Logger = Logger(this.getClass)

  case class formAnswer(examId:UUID,questionsNumber:Int)

  def getExamList()=Action{
    LOGGER.info("Starting getting Total Exams")
    val lstExams=exs.getAllExams()
    val lstExamsMap=lstExams.map(
      x=>Map(
        "id" -> x.Id.toString,
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
      val v1 = (json \ "examId").as[UUID]
      val v2 = (json \ "questions").as[Int]
      JsSuccess(formAnswer(v1, v2))
    }
    val json = request.body.asJson.get
    val frmAnsw = json.as[formAnswer](reads)
    val assessment=exs.createAssessment(frmAnsw.examId,frmAnsw.questionsNumber,"CandidateFakeName")
    Ok(Json.toJson(Map("assessmentId"->assessment.Id)))
  }

  def getQuestionByAssessment(assessmentId:UUID,questionsId:Int)=Action{req =>
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

  def loadAssessment(assessmentId:UUID)=Action {
    val assessment=exs.loadAssessment(assessmentId)
    Ok(Json.toJson(Map("questionId"->assessment.candidateAnswers.maxBy(x => x.Id).Id.toString)))
  }

  def checkAnswerByAssessment(assessmentId:UUID,questionsId:Int)=Action { request =>
    val json = request.body.asJson.get
    val userAnswersRes=(json \ "answers").as[Seq[String]].toList
    if (userAnswersRes.isEmpty)
      Ok(Json.toJson(Map("isCorrect"->false)))
    val candAnsws=CandidateAnswer(questionsId,userAnswersRes,Correct = false)
    val correct=exs.checkUserAnswer(assessmentId,questionsId,candAnsws)
    Ok(Json.toJson(Map("isCorrect"->correct)))
  }

  private def getAnswersFromJson(assessmentId:UUID,jsValue:JsValue):Map[Int,totalAnswers]={
    def convertJson2Answers(jsValue: JsValue):Map[Int,totalAnswers]={
      def v2I(jsValue: JsValue):Int={
        jsValue.asInstanceOf[JsString].value.toInt
      }
      def v2Lst(jsValue: JsValue):List[String]={
        val aa=jsValue.asInstanceOf[JsArray].value.map(x=> x.as[String].charAt(0).toString).toList
        aa
      }
      val userAnswersRes:Map[Int,totalAnswers] = jsValue.as[List[JsArray]].map(x=> v2I(x.value(0))  -> totalAnswers(v2I(x.value(0)),v2Lst(x.value(1)))).toMap
      userAnswersRes
    }

    if ((jsValue \ "answers").isEmpty){
      Map[Int,totalAnswers]()
    }
    else{
      convertJson2Answers((jsValue \ "answers").get)
    }

  }

  def collectAnswers(assessmentId:UUID)=Action { request =>
    val json = request.body.asJson.get
    if ((json \ "answers").isEmpty){
      NotAcceptable("Incorrect Json Format")
    }
    val userAnswersRes=getAnswersFromJson(assessmentId,json)
    if (userAnswersRes.isEmpty)
      Ok(Json.toJson(Map("verifiedAnswered"->0)))

    userAnswersRes.foreach(x=>exs.checkUserAnswer(assessmentId,x._1,CandidateAnswer(x._1,x._2.Answers,false)))
    if (userAnswersRes.size!=exs.getTotalQuestionsInAssessment(assessmentId)){
      exs.saveAssessment(assessmentId)
    }
    Ok(Json.toJson(Map("verifiedAnswered"->userAnswersRes.size)))
  }

  def getAssessmentReportInfo(assessmentId:UUID,prop:Int)=Action {
    val timeInSeconds= exs.getAssessmentInfo(assessmentId,prop).toString
    Ok(Json.toJson(Map("timeinseconds"->timeInSeconds)))
  }

  def getAssessmentReport(assessmentId:UUID)=Action{ request =>
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

  def getAssessmentQuestions(assessmentId:UUID)=Action {
    case class AssessmentWithAnswers(TotalQuestions:Int,AnswersUser:Map[Int,Seq[String]])
    implicit val assessmentWithAnswersWrite: Writes[AssessmentWithAnswers] = (
      (JsPath \ "TotalQuestions").write[Int] and
        (JsPath \ "AnswersUser").write[Map[Int,Seq[String]]]
      )(unlift(AssessmentWithAnswers.unapply))
    val userAns=exs.loadAssessment(assessmentId).candidateAnswers.map(x=>x.Id -> x.placeHolders).toMap
    val assessmentWithAnswers=AssessmentWithAnswers(exs.getTotalQuestionsInAssessment(assessmentId),userAns)
    Ok(Json.toJson(assessmentWithAnswers))
  }

  def getAllAssessment()=Action {
    case class AssessmentInfo(Id:UUID,Code:String,Started:String,QuestionsNumber:Int)
    implicit val AssessmentInfoWrites: Writes[AssessmentInfo] = (
      (JsPath \ "Id").write[UUID] and
        (JsPath \ "Code").write[String] and
        (JsPath \ "Started").write[String] and
        (JsPath \ "QuestionsNumber").write[Int]
    )(unlift(AssessmentInfo.unapply))
    val assessments=exs.getAllAssessment()
    val lstAssessmentIds=assessments.map(x=>AssessmentInfo(x.Id,x.exam.properties.Code,x.startTime.toString,x.exam.listQuestion.length))
    Ok(Json.toJson(lstAssessmentIds))
  }

  def importExam()= Action(parse.temporaryFile) { request =>
    val tmpFolder=System.getProperty("java.io.tmpdir")
    val file2process = new File(s"${tmpFolder}file2Import.json")
    request.body.moveTo(file2process, replace = true)
    val examImported=exs.importExamByFile(file2process).getOrElse(new Exam())
    val res:Map[String,String]=Map("message"->"File imported successfully","questions" -> examImported.listQuestion.length.toString,"code" -> examImported.properties.Code)
    Ok(Json.toJson(res))
  }

  def getVersion()=Action {
    Ok(Json.toJson(Map("version"->exs.getVersion())))
  }
}
