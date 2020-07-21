package services

import javax.inject.Singleton
import java.io.File

import models.{Assessment, Exam, _}
import java.time.{LocalDate, LocalTime}
import java.util.UUID

import play.api.{Configuration, Logger, Logging}
import play.api.libs.json.Reads._
import play.api.libs.json._
import repositories.RepoExamSimulator

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, SortedSet}
import scala.io.Source


trait GenExamSimulator extends Logging {
  def exploreExamsFromPath(folder: String,extension:String): List[Exam]
  def exploreExamsFromDb(data:dataDb):List[Exam]
  def createAssessment(examId: UUID,questionsNumber:Int,candidate:String):Assessment
  def getQuestionInAssessment(assessmentId:UUID,questionId:Int): (Assessment,Question)
  def checkUserAnswer(assessmentId:UUID,questionId:Int,candAnsw:CandidateAnswer):Boolean
  def getReportOnAssessment(assessmentId:UUID):List[CandidateAnswerReport]
  def getTotalQuestionaInAssessment(assessmentId:UUID):Int
  def getAssessmentInfo(assessmentId:UUID,prop:Int):Any
  def saveAssessment(assessmentId:UUID):Long
  def getAllAssessment():List[Assessment]
  def loadAssessment(assessmentId:UUID):Assessment
}

@Singleton
class ExamSimulator
  extends GenExamSimulator {

  private var availableExams:List[Exam] =List[Exam]()

  private val availableAssessment:ListBuffer[Assessment] =ListBuffer[Assessment]()

  private val repoExamSimulator= new RepoExamSimulator()

  private def getExamFolder(relFolder:String):File={
    val examFolder=new File(new File(".").getAbsolutePath()).getCanonicalPath()+"/"+relFolder
    logger.info(s"Exploring $examFolder .. looking for exams")
    new File(examFolder)
  }

  def exploreExamsFromDb(db:dataDb):List[Exam]={
    repoExamSimulator.connect(db)
    availableExams=repoExamSimulator.getAllAvailableExams
    availableExams
  }

  private def loadExamFromFile(fileName:File,valueIndex:Int):Exam={
    def mapAnswer(value:JsValue):PossibleAnswer={
      PossibleAnswer((value \ "placeHolder").as[String].take(1),(value \ "choiceValue").as[String])
    }
    def mapJson2Question(value:JsValue,valIndex:Int):Question={
      val q=(value \ "question").as[String]
      val choices=value("choices").as[JsArray].value.map(x=>mapAnswer(x)).toList
      val answers=value("answers").as[JsArray].value.map(x=>x.as[String]).toList
      val ref=(value \ "reference").asOpt[String].getOrElse("")
      val valid=(value \ "valid").as[Boolean]
      Question(valIndex,q,choices,answers,ref,valid)
    }
      var exam=new Exam()
      val src=Source.fromFile(fileName)

      try{
        val json=Json.parse(src.getLines().mkString)
        val title=(json \ "Title").validate[String].getOrElse("")
        val code=(json \ "Code").validate[String].getOrElse("")
        val version=(json \ "Version").validate[String].getOrElse("")
        val timeLimit=(json \ "Timelimit").validate[Int].getOrElse(0)
        val instruction=(json \ "Instructions").validate[String].getOrElse("")
        val questionsJson= (json \ "Questions").as[List[JsValue]]
        val questions:List[Question]=questionsJson.zipWithIndex.map({case (x,valIndex) =>mapJson2Question(x,valIndex)})
        val prop=ExamProperties(title,code,version,timeLimit,instruction)
          exam=Exam(UUID.randomUUID(),prop,questions)
      }finally {
        src.close()
      }

    exam
  }

  def exploreExamsFromPath(folder: String,extension:String): List[Exam] = {
    def extractExtension(filename:String):String=filename.substring(filename.lastIndexOf('.')+1)

    val d = getExamFolder(folder)
    if (d.exists && d.isDirectory) {
      val filesInFolder:List[File]=d.listFiles.filter(_.isFile).filter(f => extractExtension(f.toString)==extension).toList
      availableExams=filesInFolder.zipWithIndex.map({case (x,valIndex)=>loadExamFromFile(x,valIndex+1)})
      availableExams
    } else {
      logger.warn(s"Cannot find the folder ${d.getCanonicalPath} . Return empty exam list")
      List[Exam]()
    }
  }

  def createExamWithNumberedQuestions(exam:Exam,questionsN:Int):Exam={
    val r = scala.util.Random
    val subsetQuestions=new mutable.LinkedHashSet[Question]()
    while (subsetQuestions.size<questionsN){
      val q=exam.listQuestion(r.nextInt(exam.listQuestion.length))
      if (q.Valid && ! subsetQuestions.exists(x=> x.Text==q.Text))
        subsetQuestions.add(Question(subsetQuestions.size+1,q.Text,q.Answers,q.CorrectAnswers,q.Explanation,q.Valid))
    }
    Exam(UUID.randomUUID(),exam.properties,subsetQuestions.toList)
  }

  override def createAssessment (examId: UUID,questionsNumber:Int, candidate: String): Assessment = {
    val examLst=availableExams.find(x=> x.Id==examId).toList
    val examWithReqQuestions=createExamWithNumberedQuestions(examLst.head,questionsNumber)
    val newA=if (examLst.nonEmpty) new Assessment(UUID.randomUUID(),examWithReqQuestions,candidate) else new Assessment()
    availableAssessment.addOne(newA)
    newA
  }

  override def getQuestionInAssessment (assessmentId: UUID, questionId: Int): (Assessment,Question) = {
    val a= availableAssessment.find(x=> x.Id==assessmentId).getOrElse(new Assessment)
    val b = a.exam.listQuestion.find(x=>x.Id==questionId).getOrElse(new Question())
    (a,b)
  }

  override def checkUserAnswer(assessmentId:UUID,questionId:Int,candAnsw:CandidateAnswer):Boolean={
    val res=getQuestionInAssessment(assessmentId,questionId)
    val isAnswersCorrect: Boolean = res._2.CorrectAnswers.intersect(candAnsw.placeHolders).length==res._2.CorrectAnswers.length
    val existingAnswer=res._1.candidateAnswers.find(x=>x.Id==questionId)
    if (existingAnswer.nonEmpty){
      res._1.candidateAnswers.remove(res._1.candidateAnswers.indexOf(existingAnswer.head))
      res._1.candidateAnswers.addOne(CandidateAnswer(questionId,candAnsw.placeHolders,isAnswersCorrect))
    }
    else{
      res._1.candidateAnswers.addOne(CandidateAnswer(questionId,candAnsw.placeHolders,isAnswersCorrect))
    }

    isAnswersCorrect
  }

  override def getReportOnAssessment (assessmentId: UUID): List[CandidateAnswerReport] = {
    def CandidateAnswer2CandidateAnswerReport(c:CandidateAnswer,e:Exam):CandidateAnswerReport={
      val t=e.listQuestion.find(x=>x.Id==c.Id).getOrElse(new Question())
      val answers=t.Answers.map(x=> s"${x.placeHolder}. ${x.Text}")
      CandidateAnswerReport(t.Id,t.Text,answers,c.placeHolders,c.Correct,t.CorrectAnswers,t.Explanation)
    }
    val assessment= availableAssessment.find(x=> x.Id==assessmentId).getOrElse(new Assessment)
    assessment.candidateAnswers.sortBy(x=> x.Id).map(x=>CandidateAnswer2CandidateAnswerReport(x,assessment.exam)).toList
  }

  override def getTotalQuestionaInAssessment (assessmentId: UUID): Int = {
    availableAssessment.find(x=> x.Id==assessmentId).getOrElse(new Assessment).exam.listQuestion.length
  }

  override def getAssessmentInfo (assessmentId: UUID,prop:Int): Any = {
    val assessment=availableAssessment.find(x=> x.Id==assessmentId).getOrElse(new Assessment)
    assessment.setUpdateTime(LocalTime .now())
    prop match {
      case 1 =>   assessment.getPassedTime()
      case _ => ""
    }

  }

  override def saveAssessment(assessmentId: UUID): Long = {
    val assessments=availableAssessment.find(x=> x.Id==assessmentId)
    if(assessments.isEmpty) -1
    val assessment=assessments.head
    if (repoExamSimulator.checkAssessmentExists(assessmentId))
      {
        val res=repoExamSimulator.updateAssessment(assessment)
        logger.info(s"upodate $res docs")
        res
      }
    else
    {
      repoExamSimulator.saveAssessment(assessment)
    }

  }

  override def getAllAssessment(): List[Assessment] = {
    repoExamSimulator.getAllAssessment
  }

  override def loadAssessment(assessmentId: UUID): Assessment = {
    val assessments=availableAssessment.find(x=> x.Id==assessmentId)
    if(assessments.isDefined)
    {
      assessments.get
    }
    else{
      repoExamSimulator.loadAssessment(assessmentId) match {
        case Right(value) => value
        case Left(value) =>  new Assessment()
      }
    }
  }
}
