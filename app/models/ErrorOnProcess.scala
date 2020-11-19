package models

import play.api.libs.json.{Json, JsonValidationError, OFormat, OWrites, Reads}

case class ErrorOnProcess (Id:Int, Message:String)

object ErrorOnProcess{
  implicit val format:OFormat[ErrorOnProcess]={
    Json.format[ErrorOnProcess]
  }
  implicit def genericWrites[T <: ErrorOnProcess]: OWrites[T] =
    format.contramap[T](c => c: ErrorOnProcess)

  implicit def genericReads[T <: ErrorOnProcess](implicit evidence: scala.reflect.ClassTag[T]): Reads[T] = format.collect[T](JsonValidationError(s"Type mismatch: ${evidence.runtimeClass.getName}")) {
    case `evidence`(t) => t
  }
}
