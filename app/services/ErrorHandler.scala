package services

import javax.inject.Singleton
import models.ErrorOnProcess
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import play.api.http.HttpErrorHandler
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsObject, Json}

import scala.concurrent._


@Singleton
class ErrorHandler extends HttpErrorHandler {

  protected val LOGGER: Logger = Logger(this.getClass)

//  private def jsonResponse(statusCode: Int, message: String): JsObject =
//    Json.obj(
//      "errors" -> Json.arr(
//        Json.obj(
//          "status" -> statusCode,
//          "detail" -> message
//        )
//      )
//    )

  def jsonSingleResponse(statusCode: Int, message: String): JsObject =
      Json.obj(
        "errorId" -> statusCode,
        "errorMsg" -> message
      )

  def jsonSingleResponse(error: ErrorOnProcess): JsObject =
    jsonSingleResponse(error.Id,error.Message)

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful( statusCode match {
      case NOT_FOUND => {
        LOGGER.error("Client Error Page not found URL=" + request)
        MovedPermanently("/start")
      }
      case _ => Status (statusCode)("A client error occurred: " + message)
    }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      InternalServerError(jsonSingleResponse(500,exception.getMessage))
    )
  }
}