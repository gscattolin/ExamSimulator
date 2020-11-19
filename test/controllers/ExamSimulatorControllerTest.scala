package controllers

import akka.stream.Materializer

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.test.FakeRequest
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{DefaultActionBuilder, EssentialAction, Result}
import play.api.test.Helpers._

class ExamSimulatorControllerTest  extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite{

  implicit lazy val materializer: Materializer = app.materializer
  val application: Application = GuiceApplicationBuilder().build()
  implicit lazy val Action                     = app.injector.instanceOf(classOf[DefaultActionBuilder])
//  val controller  = app.injector.instanceOf[ExamSimulatorController]()


//  "Example Page#index" should {
//    "should be valid" in {
//
//      val action: EssentialAction = Action { request =>
//        val value = (request.body.asJson.get \ "field").as[String]
//        Ok(value)
//      }
//
//      //val request = FakeRequest(GET, "/api/exam").withJsonBody(Json.parse("""{ "field": "value" }"""))
//      val request = FakeRequest(GET, "/api/exam")
//      val result = call(action, request)
//
//      status(result) mustEqual OK
//      contentAsString(result) mustEqual "value"
//    }
//  }
}

