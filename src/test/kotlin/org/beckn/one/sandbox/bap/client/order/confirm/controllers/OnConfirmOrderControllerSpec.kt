package org.beckn.one.sandbox.bap.client.order.confirm.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.auth.model.User
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.order.confirm.services.OnConfirmOrderService
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientConfirmResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolOrderFactory
import org.beckn.one.sandbox.bap.message.mappers.OnOrderProtocolToEntityOrder
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.beckn.protocol.schemas.ProtocolOnConfirmMessage
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnConfirmOrderControllerSpec @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val protocolClient: ProtocolClient,
  private val mockMvc: MockMvc,
  private val mapping: OnOrderProtocolToEntityOrder,
  private val onConfirmOrderService: OnConfirmOrderService
) : DescribeSpec() {
  val context = contextFactory.create()
  private val protocolOnConfirm = ProtocolOnConfirm(
    context,
    message = ProtocolOnConfirmMessage(
      order = ProtocolOrderFactory.create(1)
    )
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnConfirm callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/response/v1/on_confirm?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(entityOnConfirmResults())))
        )
        val onConfirmCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_confirm_order")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onConfirmCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on confirm responses in body") {
          val results = onConfirmCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientConfirmResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnConfirm, ClientConfirmResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onConfirmPollController = OnConfirmOrderController(mockOnPollService, contextFactory, protocolClient,mapping,onConfirmOrderService)
        it("should respond with failure") {
          val response = onConfirmPollController.onConfirmOrderV1(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnConfirmResults(): List<ProtocolOnConfirm> {
    return listOf(
      protocolOnConfirm,
      protocolOnConfirm
    )
  }
}