package models


import java.time.temporal.ChronoUnit
import java.time.{LocalTime}
import java.util.UUID


import scala.collection.mutable.ListBuffer



trait BaseUniqueItem {
  val Id:UUID
}


case class Exam(Id:UUID,properties: ExamProperties,listQuestion:List[Question]) extends  BaseUniqueItem {
  def this()=this(java.util.UUID.randomUUID(),new ExamProperties(),List[Question]())
}

class Assessment(val Id:UUID, val exam: Exam, val candidate:String) extends BaseUniqueItem{
  val startTime:LocalTime =LocalTime .now
  val candidateAnswers:ListBuffer[CandidateAnswer]=new ListBuffer[CandidateAnswer]
  private var lastUpdateTime:LocalTime =LocalTime .now


  def getCorrectAnswers:Int= {
    candidateAnswers.count(_.Correct)
  }

  def setUpdateTime(t:LocalTime )={
    lastUpdateTime=t
  }

  def getPassedTime():Long={
    ChronoUnit.SECONDS.between(startTime,lastUpdateTime)
  }

  def this()=this(java.util.UUID.randomUUID(),new Exam(),"")

}

case class ExamProperties(Title:String,Code:String,Version:String,TimeLimit:Int,Instructions:String){
  def this() = this("", "","",0,"")
}

case class PossibleAnswer(placeHolder:String,Text:String)

case class Question(Id:Int,Text:String,Answers:List[PossibleAnswer],CorrectAnswers:List[String],Explanation:String,Valid:Boolean)
{
  def this() = this(0,"", List(),List(),"",false)
}

abstract class CandidateAnswerBase {
  def Id:Int
  def placeHolders:List[String]
  def Correct:Boolean
}

case class CandidateAnswer(Id:Int,placeHolders:List[String], Correct:Boolean) extends CandidateAnswerBase

case class CandidateAnswerReport(Id:Int,Text:String,Answers:List[String],placeHolders:List[String], Correct:Boolean,correctPlaceHolders:List[String],Explanation:String)
  extends CandidateAnswerBase

case class totalAnswers(Id:Int,Answers:List[String])



