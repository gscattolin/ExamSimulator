package repositories

import java.util.UUID

import models._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import play.api.Logging


import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}

class RepoExamSimulator extends Logging {

  //  var tableMap: Map[String, MongoCollection[Document]] =Map[String,MongoCollection[Document]]()
  var database: MongoDatabase = _
  var mainTable: MongoCollection[Exam] = _
  var examRep: MongoCollection[Document] = _

  val maxTimeout: FiniteDuration = Duration(10, java.util.concurrent.TimeUnit.SECONDS)
  //  var examRep:Repository[ExamDb]=_
  //  var questionsRep:Repository[Question]=_

  //  private val examsCodecRegistry = fromRegistries(fromProviders(classOf[ExamDb]), DEFAULT_CODEC_REGISTRY)
  //  private val questionsCodecRegistry = fromRegistries(fromProviders(classOf[Question]), DEFAULT_CODEC_REGISTRY)


  def connect(db: dataDb): MongoDatabase = {
    if (database == null) {
      val uri: String = s"mongodb://${db.user}:${db.pwd}@${db.host}/"
      val cl = MongoClient(uri)
      database = cl.getDatabase(db.mainDb)
      //      examRep= new Repository[ExamDb](database.getCollection[ExamDb](db.mainTable))
      examRep = database.getCollection(db.mainTable)
    }
    database
  }

  //  def getAllAvailableExams():List[Exam] = {
  //    examRep.getAll() match {
  //      case Right(examDbLst) => examDbLst.map(examDb=> {
  //        questionsRep= new Repository[Question](database.getCollection[Question](examDb.QuestionDbName))
  //        questionsRep.getAll() match {
  //          case Right(questionsLst) => Exam(UUID.randomUUID(),ExamProperties(examDb.Title,examDb.Code,examDb.Version,examDb.TimeLimit,examDb.Instructions),questionsLst)
  //          case Left(v) =>{ logger.error(s"Error reading questions db  ${examDb.QuestionDbName} ErrorId= $v") ; new Exam()}
  //        }
  //      })
  //      case Left(v) =>{ logger.error("Error reading exams ErrorId="+v) ;List[Exam]()}
  //    }
  //  }

  def getAllAvailableExams: List[Exam] = {
    Try(Await.ready(examRep.find().toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => v.map(mapDoc2Exam).toList
        case Failure(exception) =>
          logger.error(s"Error on  getAllAvailableExams $exception")
          List[Exam]()
      }
      case Failure(exception) => {
        logger.error(s"Error on  getAllAvailableExams $exception")
        List[Exam]()
      }
    }

  }

  def updateExamId(exam:Exam): Unit ={
    val res=examRep.updateOne(Document("Code" -> exam.properties.Code),Document("$set" -> Document("Id" -> exam.Id.toString))).toFuture()
    Try(Await.ready(res, maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) => logger.info(s"Update success $v")
        case Failure(exception) =>
          logger.error(s"Error on  updateExamId $exception")
      }
      case Failure(exception) => {
        logger.error(s"Error on  updateExamId $exception")
      }
    }
  }

  def importExam(file: java.io.File): Int = ???

  implicit def b2s(v: BsonValue): String = {
    v.asString().getValue
  }

  implicit def b2i(v: BsonValue): Int = {
    v.asInt32().getValue
  }


  private def mapDoc2Question(doc: Document): Question = {
    val rr = if (doc("reference").isNull) "" else doc("reference").asString().getValue
    val arr = doc("answers").asArray()
    var answ = ArrayBuffer[String]()
    for (ch <- 0 until arr.size()) {
      answ += arr.get(ch).asString()
    }
    val valid = if (doc.contains("valid")) doc("valid").asBoolean().getValue else true
    Question(doc("number").asInt32().getValue, doc("question"), mapDoc2PossibleAnswer(doc("choices")),
      answ.toList, rr, valid)
  }

  private def mapDoc2PossibleAnswer(doc: BsonValue): List[PossibleAnswer] = {
    var qs = ArrayBuffer[PossibleAnswer]()
    for (ch <- 0 until doc.asArray().size()) {
      val vv = doc.asArray().get(ch).asDocument()
      qs += PossibleAnswer(vv.get("placeHolder"), vv.get("choiceValue"))
    }
    qs.toList
  }

  private def mapDoc2Exam(doc: Document): Exam = {
    val questDb: String = doc("Q").toLowerCase
    val qColl = database.getCollection(questDb)
    var questions = List[Question]()
    Try(Await.ready(qColl.find().toFuture(), maxTimeout)) match {
      case Success(f) => f.value.get match {
        case Success(v) =>
          logger.debug(s"Found ${v.size} question inside collection $questDb")
          questions = v.map(mapDoc2Question).toList
        case Failure(exception) =>
          logger.error(s"Error on  mapDoc2Exam $exception")
          List[Exam]()
      }
      case Failure(exception) =>
        logger.error(s"Error on  mapDoc2Exam $exception")
        List[Exam]()
    }
    Exam(UUID.fromString(doc("Id")), ExamProperties(doc("Title"), doc("Code"), doc("Version"), doc("TimeLimit"), doc("Instructions")), questions)
  }
}
