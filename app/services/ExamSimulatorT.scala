package services

import java.util.UUID

import models.SourceType.SourceType
import models.{Assessment, CandidateAnswer, CandidateAnswerReport, Exam, Question}
import play.api.Logging


trait GenExamSimulator extends Logging {
  def initService():SourceType
  def getAllExams(): List[Exam]
  def createAssessment(examId: UUID,questionsNumber:Int,candidate:String):Assessment
  def getQuestionInAssessment(assessmentId:UUID,questionId:Int): (Assessment,Question)
  def checkUserAnswer(assessmentId:UUID,questionId:Int,candAnsw:CandidateAnswer):Boolean
  def getReportOnAssessment(assessmentId:UUID):List[CandidateAnswerReport]
  def getTotalQuestionsInAssessment(assessmentId:UUID):Int
  def getAssessmentInfo(assessmentId:UUID,prop:Int):Any
  def saveAssessment(assessmentId:UUID):Long
  def getAllAssessment():List[Assessment]
  def loadAssessment(assessmentId:UUID):Assessment
}
