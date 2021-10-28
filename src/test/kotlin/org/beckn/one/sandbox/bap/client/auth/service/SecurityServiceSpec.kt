package org.beckn.one.sandbox.bap.client.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import org.beckn.one.sandbox.bap.auth.service.SecurityService
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.collections.HashMap


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class SecurityServiceSpec : DescribeSpec() {
  private val securityService: SecurityService = mock(SecurityService::class.java)
  private val httpServletRequest: HttpServletRequest = mock(HttpServletRequest::class.java)

  init {
    describe("security-service") {
      // define the headers you want to be returned
      val headers: MutableMap<String?, String> = HashMap()
      headers[null] = "HTTP/1.1 200 OK"
      headers["Content-Type"] = "text/html"
      headers["Authorization"] = "Bearer abcdefghijklmnopqrstuv12121212@31313"
      val headerNames: Enumeration<String> = Collections.enumeration(headers.keys)

      it("should return null when header has no authorized bearer token") {
        `when`(securityService.getBearerToken(httpServletRequest)).thenReturn(null)
      }

      it("should return token when header has authorized bearer token") {
        `when`(httpServletRequest.headerNames).thenReturn(headerNames)
         `when`(securityService.getBearerToken(httpServletRequest)).thenReturn("abcdefghijklmnopqrstuv12121212@31313")
      }
    }
  }
}