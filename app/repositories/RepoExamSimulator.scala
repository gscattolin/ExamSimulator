package repositories

import java.io.File
import java.util.UUID

import models.{Assessment, _}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonValue}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase, bson}
import play.api.Logger
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.io.Source
import scala.util.{Failure, Success, Try}

class RepoExamSimulator(){

  //  var tableMap: Map[String, MongoCollection[Document]] =Map[String,MongoCollection[Document]]()
  var database: MongoDatabase = _
  var examRep: MongoCollection[Document] = _
  var assessmentRep: MongoCollection[Document] = _

  val maxTimeout: FiniteDuration = Duration(10, java.util.concurrent.TimeUnit.SECONDS)

  protected val LOGGER: Logger = Logger(this.getClass)


  def loadExamFromFile(fileName:File):Exam={
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
      var uid=UUID.randomUUID()
      if ((json \ "Id").isDefined) {
        uid=UUID.fromString((json \ "Id").validate[String].get)
      }
      val title=(json \ "Title").validate[String].getOrElse("")
      val code=(json \ "Code").validate[String].getOrElse("")
      val version=(json \ "Version").validate[String].getOrElse("")
      val timeLimit=(json \ "TimeLimit").validate[Int].getOrElse(0)
      val instruction=(json \ "Instructions").validate[String].getOrElse("")
      val questionsJson= (json \ "Questions").as[List[JsValue]]
      val questions:List[Question]=questionsJson.zipWithIndex.map({case (x,valIndex) =>mapJson2Question(x,valIndex+1)})
      val prop=ExamProperties(title,code,version,timeLimit,instruction)
      exam=Exam(uid,prop,questions)
    }finally {
      src.close()
    }

    exam
  }

  def getExamFolder(relFolder:String):File={
    val examFolder=new File(new File(".").getAbsolutePath).getCanonicalPath+"/"+relFolder
    LOGGER.debug(s"Exploring $examFolder .. looking for exams")
    new File(examFolder)
  }

  def getAllAvailableExamsFromFolder(folder:String,extension:String): List[Exam] = {
    def extractExtension(filename:String):String=filename.substring(filename.lastIndexOf('.')+1)
    val d = getExamFolder(folder)
    if (d.exists && d.isDirectory) {
      val filesInFolder:List[File]=d.listFiles.filter(_.isFile).filter(f => extractExtension(f.toString)==extension).toList
      val availableExams=filesInFolder.map({x=>loadExamFromFile(x)})
      availableExams
    } else {
      LOGGER.warn(s"Cannot find the folder ${d.getCanonicalPath} . Return empty exam list")
      List[Exam]()
    }
  }

  def connect(db: dataDb): MongoDatabase = {
    if (database == null) {
      val uri: String = s"mongodb://${db.user}:${db.pwd}@${db.host}/"
      val cl = MongoClient(uri)
      database = cl.getDatabase(db.mainDb)
      //      examRep= new Repository[ExamDb](database.getCollection[ExamDb](db.mainTable))
      examRep = database.getCollection(db.mainTable)
      assessmentRep= database.getCollection(db.assessmentTable)
    }
    database
  }


  def getAllAvailableExamsFromDb: List[Exam] = {
    Try(Await.ready(examRep.find().toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => v.map(mapDoc2Exam).toList
        case Failure(exception) =>
          LOGGER.error(s"Error on  getAllAvailableExams $exception")
          List[Exam]()
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  getAllAvailableExams $exception")
        List[Exam]()
    }

  }

  def getAllAssessment: List[Assessment] = {
    Try(Await.ready(assessmentRep.find[Document]().toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => val assessments=v.map(mapDoc2Assessment).toList ; LOGGER.debug(s"Loaded ${assessments.length} assessments"); assessments
        case Failure(exception) =>
          LOGGER.error(s"Error on  getAllAssessment $exception")
          List[Assessment]()
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  getAllAssessment $exception")
        List[Assessment]()
    }
  }

  def saveAssessment(assessment: Assessment):Long={
    applyFutureSingle(assessmentRep.insertOne(mapAssessment2Doc(assessment)).toFuture())
  }

  def loadAssessment(assessmentId: UUID):Either[Int,Assessment]= {
    Try(Await.ready(assessmentRep.find[Document](Document("Id" -> assessmentId.toString)).toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => if (v.length>1) LOGGER.warn(s"Find more than one Assessment matches the condition Id= $assessmentId"); Right(mapDoc2Assessment(v.head))
        case Failure(exception) =>
          LOGGER.error(s"Error on  loadAssessment $exception")
          Left(-2)
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  loadAssessment $exception")
        Left(-3)
    }
  }

  def checkAssessmentExists(assessmentId: UUID):Boolean={
    val f=assessmentRep.countDocuments(Document("Id" -> assessmentId.toString)).toFuture()
    applyFutureSingle(f)>0

  }

  def updateAssessment(assessment: Assessment):Long= {
    val f=assessmentRep.updateOne(Document("Id" -> assessment.Id.toString),
      Document("$set" -> Document("candidateAnswers" -> mapCandidateAnswer2Doc(assessment.candidateAnswers)))).toFuture()
    applyFutureSingle(f)
  }

  def deleteAssessment(assessment: Assessment):Long={
    val f= assessmentRep.deleteOne(Document("Id" -> assessment.Id.toString)).toFuture()
    applyFutureSingle(f)
  }

  def loadSingleExamByCode(code:String):Exam={
    val f=examRep.find(Document("Code" ->code.toUpperCase)).toFuture()
    val h=new Exam()
    Try(Await.ready(f, maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => LOGGER.info(s"operation success $v"); if (v.nonEmpty) mapDoc2Exam(v.head) else h
        case Failure(exception) =>
          LOGGER.error(s"Error  $exception")
         h
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  getAllAvailableExams $exception")
        h
    }
  }

  def deleteExam(exam: Exam):Long={
    val qC=s"Q-${exam.properties.Code}".toLowerCase
    val collection=database.getCollection(qC)
    applyFutureSingle(collection.drop().toFuture())
    val f= examRep.deleteOne(Document("Id" -> exam.Id.toString)).toFuture()
    applyFutureSingle(f)
  }

  def updateExamId(exam:Exam): Long ={
    val f=examRep.updateOne(Document("Code" -> exam.properties.Code),Document("$set" -> Document("Id" -> exam.Id.toString))).toFuture()
    applyFutureSingle(f)
  }

  def importExamFromFile2Mongo(file: java.io.File): Either[Int,Exam] = {
    if (!file.isFile) {
      LOGGER.error(s"Cannot load file ${file.getName} for importing")
      return Left(-1)
    }
    val exam=loadExamFromFile(file)
    saveExam(exam)
    Right(exam)
  }

  implicit def b2s(v: BsonValue): String = {
    v.asString().getValue
  }

  implicit def b2i(v: BsonValue): Int = {
    v.asInt32().getValue
  }

  private def AnyRefWrap(v:Any):Long={
    v match{
      case _:Completed => 1
      case x:DeleteResult => x.getDeletedCount
      case x:UpdateResult => x.getModifiedCount
      case x:Long => x.toInt
    }

  }

  private def applyFutureSingle(f:Future[Any]):Long={
    Try(Await.ready(f, maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => LOGGER.info(s"operation success $v"); AnyRefWrap(v)
        case Failure(exception) =>
          LOGGER.error(s"Error  $exception")
          -2
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  getAllAvailableExams $exception")
        -3
    }
  }

  private def applyFutureSequence(f:Future[Seq[Any]]):Seq[Any]={
    Try(Await.ready(f, maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => v
        case Failure(exception) =>
          LOGGER.error(s"Error on  getAllAvailableExams $exception")
          List[Completed]()
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  getAllAvailableExams $exception")
        List[Completed]()
    }
  }

  private def Array2List(doc:BsonValue):List[String]={
    val res=ListBuffer[String]()
    val iter=doc.asArray()
      for ( ch <- 0 until iter.size())
        res.addOne(iter.get(ch).asString())
    res.toList
  }

  private def mapDoc2Assessment(doc: Document):Assessment= {
    val assessment=new Assessment(UUID.fromString(doc("Id")),mapDoc2ExamInAssessment(doc("exam").asDocument()),doc("candidate"))
    assessment.candidateAnswers.addAll(mapDoc2CandidateAnswer(doc("candidateAnswers")))
    assessment
  }

  private def mapAssessment2Doc(assessment: Assessment ):Document={
    Document("Id" -> assessment.Id.toString,"startTime" -> assessment.startTime.toString,"candidate" -> assessment.candidate,"exam" -> mapExamAssessment2Doc(assessment.exam),
    "candidateAnswers" -> mapCandidateAnswer2Doc(assessment.candidateAnswers))

  }

  private def mapCandidateAnswer2Doc(cas:ListBuffer[CandidateAnswer]):BsonArray={
    val res = new BsonArray()
    for (v <- cas) res.add(BsonDocument("Id" -> v.Id, "placeHolders" -> v.placeHolders, "Correct" -> v.Correct ))
    res
  }

  private def mapDoc2CandidateAnswer(value:BsonValue):ListBuffer[CandidateAnswer]={
    val arr=ListBuffer[CandidateAnswer]()
    val lst=value.asArray()
    for (ch <- 0 until lst.size()) {
      val valEl:Document=lst.get(ch).asDocument()
      arr.addOne(CandidateAnswer(valEl("Id").asInt32(),Array2List(valEl("placeHolders")),valEl("Correct").asBoolean().getValue))
    }
    arr
  }

  private def mapDoc2Question(doc: Document): Question = {
    val rr = if (doc("reference").isNull) "" else doc("reference").asString().getValue
    val valid = if (doc.contains("valid")) doc("valid").asBoolean().getValue else true
    Question(doc("number").asInt32().getValue, doc("question"), mapDoc2PossibleAnswer(doc("choices")),
      Array2List(doc("answers")), rr, valid)
  }

  private def mapQuestion2Doc(q:Question):Document={
    Document("number"->q.Id,"question" -> q.Text,"choices" -> mapPossibleAnswer2Doc(q.Answers),"answers" -> q.CorrectAnswers,"reference"->q.Explanation,"valid"->q.Valid)
  }

  private def mapDoc2PossibleAnswer(doc: BsonValue): List[PossibleAnswer] = {
    var qs = ArrayBuffer[PossibleAnswer]()
    for (ch <- 0 until doc.asArray().size()) {
      val vv = doc.asArray().get(ch).asDocument()
      qs += PossibleAnswer(vv.get("placeHolder").asString().getValue.head.toString, vv.get("choiceValue"))
    }
    qs.toList
  }

  private def mapPossibleAnswer2Doc(pAnswers: List[PossibleAnswer]): BsonArray ={
    val bsonArray = new BsonArray()
    for (v <- pAnswers) bsonArray.add(BsonDocument("placeHolder"->v.placeHolder.charAt(0).toString,"choiceValue" -> v.Text))
    bsonArray
  }

  private def mapExamAssessment2Doc(examInAssessment:Exam):Document= {
    val exam=examInAssessment
    val bsonArray = new BsonArray()
    for (q <- examInAssessment.listQuestion) bsonArray.add(mapQuestion2Doc(q).toBsonDocument)
    Document("Id" -> exam.Id.toString,"Title" -> exam.properties.Title,"Code" -> exam.properties.Code,"Version" -> exam.properties.Version
    ,"TimeLimit" -> exam.properties.TimeLimit,"Instructions" -> exam.properties.Instructions,"Questions" -> bsonArray)
  }

  private def mapDoc2ExamInAssessment(doc: Document): Exam = {
    val qs = ArrayBuffer[Question]()
    val arrQ=doc("Questions").asArray()
    for (ch <- 0 until arrQ.size()) qs.addOne(mapDoc2Question(arrQ.get(ch).asDocument()))
    val timeL:Int=doc("TimeLimit").asInt32()
    Exam(UUID.fromString(doc("Id")), ExamProperties(doc("Title"), doc("Code"), doc("Version"),timeL , doc("Instructions")), qs.toList)
  }

  private def mapDoc2Exam(doc: Document): Exam = {
    val questDb: String = doc("Q").toLowerCase
    val qColl = database.getCollection(questDb)
    var questions = List[Question]()
    Try(Await.ready(qColl.find().toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) =>
          LOGGER.debug(s"Found ${v.size} question inside collection $questDb")
          questions = v.map(mapDoc2Question).toList
        case Failure(exception) =>
          LOGGER.error(s"Error on  mapDoc2Exam $exception")
          List[Exam]()
      }
      case Failure(exception) =>
        LOGGER.error(s"Error on  mapDoc2Exam $exception")
        List[Exam]()
    }
    Exam(UUID.fromString(doc("Id")), ExamProperties(doc("Title"), doc("Code"), doc("Version"), doc("TimeLimit"), doc("Instructions")), questions)
  }

  private def saveExam(exam:Exam): Int = {
  val questionsColl=s"q-${exam.properties.Code.toLowerCase}"
   val newExam= Document("Id" -> exam.Id.toString,"Title" -> exam.properties.Title,"Code" -> exam.properties.Code,"Version" -> exam.properties.Version
      ,"TimeLimit" -> exam.properties.TimeLimit,"Instructions" -> exam.properties.Instructions,"Q" -> questionsColl)
    val questionsSeq= for (q <- exam.listQuestion) yield mapQuestion2Doc(q)
    val existingCollections=applyFutureSequence(database.listCollectionNames().toFuture()).map(x=> x.toString).toList
    if(existingCollections.contains(questionsColl)){
      LOGGER.warn(s"Deleting all existing documents of questions $questionsColl")
      applyFutureSingle(database.getCollection(questionsColl).deleteMany(Document()).toFuture())
    }
    else{
      applyFutureSingle(database.createCollection(questionsColl).toFuture())
    }
    val resK=applyFutureSingle(database.getCollection(questionsColl).insertMany(questionsSeq).toFuture())
    if (resK>0){
      val fe=examRep.insertOne(newExam).toFuture()
      applyFutureSingle(fe)
      }
    1
  }

}
