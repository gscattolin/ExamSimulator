package repositories

import java.util.UUID

import models.BaseUniqueItem
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.bson.BSONObjectID


case class ExamDb(Id:UUID,Title:String,Code:String,Version:String,TimeLimit:Int,Instructions:String,QuestionDbName:String) extends BaseUniqueItem




