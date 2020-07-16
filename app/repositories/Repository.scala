//package repositories
//
//import java.util.UUID
//
//import models.{BaseUniqueItem, Exam}
//import org.mongodb.scala._
//import org.mongodb.scala.model.Filters._
//import play.api.Logging
//
//import scala.concurrent.duration.{Duration, FiniteDuration}
//import scala.concurrent.{Await, Future}
//import scala.reflect.ClassTag
//import scala.util.{Failure, Success, Try}
//
//trait RepositoryT[T]{
//  def create(item:T):Option[T]
//  def update(item:T):Int
//  def delete(Id:UUID):Either[Int,UUID]
//  def getById(Id:UUID):Either[Int,T]
//  def getAll():Either[Int,List[T]]
//}
//
// class Repository[T <: ClassTag[T]](collection:MongoCollection[T]) extends RepositoryT[T] with Logging{
///
//  val maxTimeout: FiniteDuration =Duration(10, java.util.concurrent.TimeUnit.SECONDS)
//
//
//  private def  getValueByFuture(f:Future[T],methodName:String):Either[Int,T]={
//    Try(Await.ready(f,maxTimeout)) match {
//      case Success(f) => f.value.get match {
//        case Success(v) => Right(v)
//        case Failure(e) =>{logger.error(s"Exception on ${methodName} = "+e);Left(-1)}
//      }
//      case Failure(e) => {logger.error(s"Timeout on ${methodName} = "+e);Left(-1)}
//    }
//  }
//
//  override def create(item: T): Option[T] = {
//    val insertObservable: Observable[Completed] = collection.insertOne(item)
//    insertObservable.toFuture()
//    insertObservable.subscribe(new Observer[Completed] {
//      override def onNext(result: Completed): Unit = logger.debug(s"onNext: $result")
//      override def onError(e: Throwable): Unit = logger.error(s"onError: $e")
//      override def onComplete(): Unit = logger.debug("onComplete")
//    })
//    Some(item)
//  }
//
//  override def update(item: T): Int = ???
//
//  override def delete(Id: UUID): Either[Int,UUID] = {
//    val f=collection.deleteOne(equal("_id",Id)).toFuture()
//    Try(Await.ready(f,maxTimeout)) match {
//      case Success(f) => f.value.get match {
//        case Success(_) => Right(Id)
//        case Failure(e) =>{logger.error(s"Exception on delete = "+e);Left(-1)}
//      }
//      case Failure(e) => {logger.error(s"Timeout on delete = "+e);Left(-1)}
//    }
//  }
//
//  override def getById(Id: UUID): Either[Int,T] = {
//    val f=collection.find(equal("_id",Id)).first().toFuture()
//    getValueByFuture(f,"getById")
//  }
//
//   override def getAll():Either[Int,List[T]] = {
//     val f=collection.find[T]().toFuture()
//     Try(Await.ready(f,maxTimeout)) match {
//       case Success(f) => f.value.get match {
//         case Success(v) => Right(v.toList)
//         case Failure(e) =>{logger.error(s"Exception on delete = "+e);Left(-1)}
//       }
//       case Failure(e) => {logger.error(s"Timeout on delete = "+e);Left(-1)}
//     }
//   }
// }
//
//object Repository{
//
////  def mapDocToExam(doc:Document):Exam={
////
////  }
////
////  def ExamToDoc(ex:Exam):Document= ???
//}
//
//
//
//
//
//
