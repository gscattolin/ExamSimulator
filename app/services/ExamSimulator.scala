package services

import javax.inject.{Inject, Singleton}

import models.{Assessment, Exam, _}
import java.time.{LocalTime}
import java.util.UUID

import models.SourceType.SourceType
import play.api.{Configuration}
import repositories.RepoExamSimulator

import scala.collection.mutable
import scala.collection.mutable.{ ListBuffer}



@Singleton
class ExamSimulator @Inject() (config:Configuration)
  extends GenExamSimulator {

  private val configDataSources:String="datasources"

  private val configDataSourcesFile:String="files"
  private val configFileFolder:String="folder"
  private val configFileExtension:String="extension"

  private val configDataSourcesDb:String="db"
  private val configDbHost:String="host"
  private val configDbUser:String="username"
  private val configDbPwd:String="password"
  private val configDbName:String="dbName"
  private val configDbMainTable:String="MainTable"
  private val configDbAssessmentTable:String="AssessmentTable"

  private val availableConfigSources:Set[String]=Set(configDataSourcesDb,configDataSourcesFile)
  private var availableExams:List[Exam] =List[Exam]()

  private val availableAssessment:ListBuffer[Assessment] =ListBuffer[Assessment]()

  private val dataSource=initService()

  lazy val repoExamSimulator= new RepoExamSimulator()

  def initService():SourceType={
    val source=config.getAndValidate(configDataSources,availableConfigSources)
    if (source==configDataSourcesDb){
      val f=config.get[Map[String,String]](configDataSourcesDb)
      val d=dataDb(f(configDbHost),f(configDbUser),f(configDbPwd),f(configDbName),f(configDbMainTable),f(configDbAssessmentTable))
      repoExamSimulator.connect(d)
      SourceType.Dbs
    }else {
      SourceType.Files
    }
  }


  private def exploreExamsFromDb():List[Exam]={
    availableExams=repoExamSimulator.getAllAvailableExamsFromDb
    availableExams
  }

  private def exploreExamsFromFolder():List[Exam]={
    val f=config.get[Map[String,String]](configDataSourcesFile)
    var folder=f(configFileFolder)
    val extension=f(configFileExtension)
    if (! System.getProperty("os.name").contains("Window")) folder=folder.replace('\\', '/')
    availableExams=repoExamSimulator.getAllAvailableExamsFromFolder(folder,extension)
    availableExams
  }


  override def getAllExams(): List[Exam] = {
    dataSource match {
      case SourceType.Files => exploreExamsFromFolder()
      case SourceType.Dbs =>  exploreExamsFromDb()
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
    val assessment= availableAssessment.find(x=> x.Id==assessmentId).getOrElse(loadAssessment(assessmentId))
    val b = assessment.exam.listQuestion.find(x=>x.Id==questionId).getOrElse(new Question())
    (assessment,b)
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

  override def getTotalQuestionsInAssessment (assessmentId: UUID): Int = {
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
        case Right(value) => logger.info(s"Loaded successfully a new assessment Id=$assessmentId");  availableAssessment.addOne(value); value
        case Left(value) =>  new Assessment()
      }
    }
  }



}
