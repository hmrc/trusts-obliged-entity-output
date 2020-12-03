package connectors

import config.AppConfig
import config.Constants._
import javax.inject.Inject
import models.{NonRepudiationServiceResponse, SuccessfulResponse}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class NonRepudiationServiceConnector @Inject()(http: HttpClient, config: AppConfig) {

  private def nrsHeaders(): Seq[(String, String)] = {
    Seq(
      X_API_KEY -> s"${config.nrsToken}",
      CONTENT_TYPE -> CONTENT_TYPE_JSON
    )
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = nrsHeaders())

  def getPdf(payload: JsValue)(implicit ec: ExecutionContext): Future[NonRepudiationServiceResponse] = {
    if (config.nrsEnabled) {
      val url: String = s"${config.nrsUrl}/generate-pdf/template/trusts-5mld-1-0-0/signed-pdf"
      http.POST[JsValue, NonRepudiationServiceResponse](url, payload)
    } else {
      Future.successful(SuccessfulResponse("some base64 encoded binary in string form"))
    }
  }

}
