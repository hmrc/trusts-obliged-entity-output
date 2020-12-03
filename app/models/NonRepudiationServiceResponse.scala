package models

import play.api.Logging
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._
import play.api.libs.json.JsValue

trait NonRepudiationServiceResponse

case class SuccessfulResponse(body: String) extends NonRepudiationServiceResponse
case class BadRequestResponse(body: JsValue) extends NonRepudiationServiceResponse
case object UnauthorisedResponse extends NonRepudiationServiceResponse
case object NotFoundResponse extends NonRepudiationServiceResponse
case object InternalServerErrorResponse extends NonRepudiationServiceResponse

object NonRepudiationServiceResponse extends Logging {

  implicit lazy val httpReads: HttpReads[NonRepudiationServiceResponse] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK =>
        SuccessfulResponse(response.body)
      case BAD_REQUEST =>
        BadRequestResponse(response.json)
      case UNAUTHORIZED =>
        UnauthorisedResponse
      case NOT_FOUND =>
        NotFoundResponse
      case _ =>
        InternalServerErrorResponse
    }
  }

}
