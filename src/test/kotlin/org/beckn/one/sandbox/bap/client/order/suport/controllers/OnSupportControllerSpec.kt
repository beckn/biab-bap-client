package org.beckn.one.sandbox.bap.client.order.suport.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.order.support.controllers.OnSupportController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSupportResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSupport
import org.beckn.protocol.schemas.ProtocolOnSupportMessage
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class OnSupportControllerSpec @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val protocolClient: ProtocolClient,
  private val mockMvc: MockMvc
) : DescribeSpec() {
  val context = contextFactory.create()
  private val protocolOnSupport = ProtocolOnSupport(
    context,
    message = ProtocolOnSupportMessage(phone = "1234567890")
  )

  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnInitialize callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/v1/on_support?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(entityOnSupportResults())))
        )
        val onSupportCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_support")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onSupportCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on support responses in body") {
          val results = onSupportCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientSupportResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnSupport, ClientSupportResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onSupportPollController = OnSupportController(mockOnPollService, contextFactory, protocolClient)
        it("should respond with failure") {
          val response = onSupportPollController.onSupportOrderV1(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnSupportResults(): List<ProtocolOnSupport> {
    return listOf(
      protocolOnSupport,
      protocolOnSupport
    )
  }

}