package org.beckn.one.sandbox.bap.client.order.cancel.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientCancelResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolOrderFactory
import org.beckn.protocol.schemas.ProtocolOnCancel
import org.beckn.protocol.schemas.ProtocolOnCancelMessage
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class OnCancelOrderControllerSpec @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val protocolClient: ProtocolClient,
  private val mockMvc: MockMvc
) : DescribeSpec() {
  val context = contextFactory.create()
  private val protocolOnCancel = ProtocolOnCancel(
    context,
    message = ProtocolOnCancelMessage(
      order = ProtocolOrderFactory.create(1)
    )
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnCancel callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/response/v1/on_cancel?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(entityOnCancelResults())))
        )
        val onCancelCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_cancel_order")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onCancelCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on cancel responses in body") {
          val results = onCancelCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientCancelResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnCancel, ClientCancelResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onCancelPollController = OnCancelOrderController(mockOnPollService, contextFactory, protocolClient)
        it("should respond with failure") {
          val response = onCancelPollController.onCancelOrderV1(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnCancelResults(): List<ProtocolOnCancel> {
    return listOf(
      protocolOnCancel,
      protocolOnCancel
    )
  }

}