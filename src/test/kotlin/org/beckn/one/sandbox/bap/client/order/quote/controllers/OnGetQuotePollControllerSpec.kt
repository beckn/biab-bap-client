package org.beckn.one.sandbox.bap.client.order.quote.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientQuoteResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.factories.ProtocolOnSelectMessageSelectedFactory
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSelect
import org.beckn.protocol.schemas.ProtocolOnSelectMessage
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnGetQuotePollControllerSpec @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val protocolClient: ProtocolClient,
  private val mockMvc: MockMvc
) : DescribeSpec() {

  private val fixedClock = Clock.fixed(
    Instant.parse("2018-11-30T18:35:24.00Z"),
    ZoneId.of("UTC")
  )

  val context = contextFactory.create()
  private val protocolOnSelect = ProtocolOnSelect(
    context,
    message = ProtocolOnSelectMessage(selected = ProtocolOnSelectMessageSelectedFactory.create())
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnGetQuote callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/v1/on_select?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(entitySelectResults())))
        )
        val onGetQuoteCall = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_get_quote")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onGetQuoteCall.andExpect(status().isOk)
        }

        it("should respond with all select responses in body") {
          val results = onGetQuoteCall.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientQuoteResponse::class.java)
          clientResponse.message?.quote shouldNotBe null
          clientResponse.message?.quote?.quote shouldBe protocolOnSelect.message?.selected?.quote
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnSelect, ClientQuoteResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onSelectPollController = OnGetQuotePollController(mockOnPollService, contextFactory, protocolClient)
        it("should respond with failure") {
          val response = onSelectPollController.onSelect(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entitySelectResults(): List<ProtocolOnSelect> {
    return listOf(
      protocolOnSelect,
      protocolOnSelect
    )
  }
}