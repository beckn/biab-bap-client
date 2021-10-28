package org.beckn.one.sandbox.bap.client.discovery.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSearchResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolCatalog
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnSearch
import org.beckn.protocol.schemas.ProtocolOnSearchMessage
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
import java.time.OffsetDateTime
import java.time.ZoneId

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnSearchPollControllerSpec @Autowired constructor(
    private val contextFactory: ContextFactory,
    private val mapper: ObjectMapper,
    private val protocolClient: ProtocolClient,
    private val mockMvc: MockMvc
) : DescribeSpec() {

  private val fixedClock = Clock.fixed(
    Instant.parse("2018-11-30T18:35:24.00Z"),
    ZoneId.of("UTC")
  )
  private val context = ProtocolContext(
    domain = "LocalRetail",
    country = "IN",
    action =  ProtocolContext.Action.SEARCH,
    city = "Pune",
    coreVersion = "0.9.1-draft03",
    bapId = "http://host.bap.com",
    bapUri = "http://host.bap.com/v1",
    bppId = "http://host.bpp.com",
    bppUri = "http://host.bpp.com/v1",
    transactionId = "222",
    messageId = "222",
    timestamp = OffsetDateTime.now(fixedClock)
  )

  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnSearch callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/response/v1/on_search?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(entitySearchResults())))
        )
        val onSearchCall = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_search")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onSearchCall.andExpect(status().isOk)
        }

        it("should respond with all search responses in body") {
          val results = onSearchCall.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientSearchResponse::class.java)
          clientResponse.message?.catalogs?.size shouldBe 2
          clientResponse.message?.catalogs?.forEach { it.bppId shouldBe context.bppId }
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnSearch, ClientSearchResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onSearchPollController = OnSearchPollController(mockOnPollService, contextFactory, protocolClient)
        it("should respond with failure") {
          val response = onSearchPollController.onSearchV1(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entitySearchResults(): List<ProtocolOnSearch> {
    val entitySearchResponse = ProtocolOnSearch(
      context = context,
      message = ProtocolOnSearchMessage(ProtocolCatalog())
    )
    return listOf(
      entitySearchResponse,
      entitySearchResponse
    )
  }
}