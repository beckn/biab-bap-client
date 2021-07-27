package org.beckn.one.sandbox.bap.client.shared.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.errors.bap.ProtocolClientError
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.message.factories.ProtocolCatalogFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolContextFactory
import org.beckn.protocol.schemas.ProtocolOnSearch
import org.beckn.protocol.schemas.ProtocolOnSearchMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class GenericProtocolClientServiceSpec @Autowired constructor(
  val protocolClientService: GenericProtocolClientService<ProtocolOnSearch>,
  val protocolClientBap: ProtocolClient,
  val mapper: ObjectMapper
) : DescribeSpec() {

  private val _onSearchStub = UUID.randomUUID()
  private val searchResponse = listOf(
    ProtocolOnSearch(
      context = ProtocolContextFactory.fixed,
      message = ProtocolOnSearchMessage(
        catalog = ProtocolCatalogFactory.create(1)
      )
    ),
    ProtocolOnSearch(
      context = ProtocolContextFactory.fixed,
      message = ProtocolOnSearchMessage(
        catalog = ProtocolCatalogFactory.create(2)
      )
    )
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()
  val messageId = "some-message-id"

  init {
    describe("Given ProtocolClient calls protocol service") {

      context("when it calls BAP protocol service and gets HTTP 200 responses for a message id") {
        mockProtocolBap.stubFor(
          get("/protocol/v1/on_search?messageId=some-message-id").withId(_onSearchStub).willReturn(okJson(mapper.writeValueAsString(searchResponse)))
        )
        val results = protocolClientService.getResponse(protocolClientBap.getSearchResponsesCall(messageId))

        it("should parse a successful response"){
          results shouldBeRight searchResponse
        }
      }

      context("when it calls BAP protocol service and gets HTTP 500 responses for a message id") {
        mockProtocolBap.editStub(
          get("/protocol/v1/on_search?messageId=some-message-id").withId(_onSearchStub).withId(_onSearchStub).willReturn(WireMock.serverError())
        )
        val results = protocolClientService.getResponse(protocolClientBap.getSearchResponsesCall(messageId))

        it("should return Internal error"){
          results shouldBeLeft ProtocolClientError.Internal
        }
      }

    }
  }


}