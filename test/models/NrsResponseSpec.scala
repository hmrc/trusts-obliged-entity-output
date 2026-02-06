/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import base.SpecBase
import org.mockito.Mockito.{mock, when}
import play.api.http.Status._
import play.api.libs.ws.WSResponse

class NrsResponseSpec extends SpecBase {

  "parseNRS400ResponseBody method" should {

    "remove data within square brackets" in {
      val input          =
        """[
          |{"message":"#/entities/beneficiary/unidentified: 2 schema violations found"},
          |{"message":"#/entities/beneficiary/unidentified/0/beneficiaryDescription: string [INVALID BENEFICIARY DESCRIPTION ***] does not match pattern ^[0-9A-Z{À-˿'}\\- \\u005C&`'^]{1,70}$"},
          |{"message":"#/entities/beneficiary/unidentified/1/beneficiaryDescription: string [INVALID BENEFICIARY DESCRIPTION 2 ***] does not match pattern ^[0-9A-Z{À-˿'}\\- \\u005C&`'^]{1,70}$"}]""".stripMargin
      val expectedResult =
        """"#/entities/beneficiary/unidentified: 2 schema violations found",
          |"#/entities/beneficiary/unidentified/0/beneficiaryDescription: string [OBFUSCATED] does not match pattern ^[0-9A-Z{À-˿'}\\- \\u005C&`'^]{1,70}$",
          |"#/entities/beneficiary/unidentified/1/beneficiaryDescription: string [OBFUSCATED] does not match pattern ^[0-9A-Z{À-˿'}\\- \\u005C&`'^]{1,70}$"""".stripMargin

      val result = NrsResponse.parseNRS400ResponseBody(input)

      result mustBe expectedResult
    }
  }

  "NrsResponse bodyReadable" should {

    "return NotFoundResponse for NOT_FOUND status" in {
      val mockResponse = mock(classOf[WSResponse])
      when(mockResponse.status).thenReturn(NOT_FOUND)
      when(mockResponse.body).thenReturn("Not found")

      val result = NrsResponse.bodyReadable.transform(mockResponse)

      result mustBe NotFoundResponse
    }

    "return UnauthorisedResponse for UNAUTHORIZED status" in {
      val mockResponse = mock(classOf[WSResponse])
      when(mockResponse.status).thenReturn(UNAUTHORIZED)

      val result = NrsResponse.bodyReadable.transform(mockResponse)

      result mustBe UnauthorisedResponse
    }

    "return ServiceUnavailableResponse for SERVICE_UNAVAILABLE status" in {
      val mockResponse = mock(classOf[WSResponse])
      when(mockResponse.status).thenReturn(SERVICE_UNAVAILABLE)

      val result = NrsResponse.bodyReadable.transform(mockResponse)

      result mustBe ServiceUnavailableResponse
    }

    "return InternalServerErrorResponse for unknown status codes" in {
      val mockResponse = mock(classOf[WSResponse])
      when(mockResponse.status).thenReturn(INTERNAL_SERVER_ERROR)

      val result = NrsResponse.bodyReadable.transform(mockResponse)

      result mustBe InternalServerErrorResponse
    }
  }

}
