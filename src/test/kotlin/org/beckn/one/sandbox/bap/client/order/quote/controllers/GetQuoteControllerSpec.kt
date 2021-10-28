package org.beckn.one.sandbox.bap.client.order.quote.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.auth.model.User
import org.beckn.one.sandbox.bap.client.shared.dtos.CartDto
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.shared.dtos.GetQuoteRequestDto
import org.beckn.one.sandbox.bap.client.shared.dtos.GetQuoteRequestMessageDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.client.factories.CartFactory
import org.beckn.one.sandbox.bap.common.Verifier
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.anotherRetailBengaluruBpp
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.registryBppLookupApi
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.retailBengaluruBpp
import org.beckn.one.sandbox.bap.common.factories.ResponseFactory
import org.beckn.one.sandbox.bap.common.factories.SubscriberDtoFactory
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class GetQuoteControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory,
    val uuidFactory: UuidFactory,
) : DescribeSpec() {
  private val verifier: Verifier = Verifier(objectMapper)

  init {
    describe("Get Quote") {
      MockNetwork.startAllSubscribers()
      val context = ClientContext(transactionId = uuidFactory.create())
      val cart = CartFactory.create(bpp1Uri = retailBengaluruBpp.baseUrl())

      beforeEach {
        MockNetwork.resetAllSubscribers()
        retailBengaluruBpp.resetAll()
        registryBppLookupApi.resetAll()
        stubBppLookupApi(registryBppLookupApi, retailBengaluruBpp)
        stubBppLookupApi(registryBppLookupApi, anotherRetailBengaluruBpp)
      }

      it("should return error when bpp select call fails") {
        retailBengaluruBpp.stubFor(post("/select").willReturn(serverError()))

        val getQuoteResponseString = invokeGetQuoteApi(context = context, cart = cart)
          .andExpect(status().isInternalServerError)
          .andReturn()
          .response.contentAsString

        val getQuoteResponse =
          verifyResponseMessage(getQuoteResponseString, ResponseMessage.nack(), BppError.Internal.error(), context)
        verifyThatBppSelectApiWasInvoked(getQuoteResponse, cart, retailBengaluruBpp)
        verifier.verifyThatSubscriberLookupApiWasInvoked(registryBppLookupApi, retailBengaluruBpp)

      }

      it("should invoke provide select api and save message") {
        retailBengaluruBpp
          .stubFor(
            post("/select").willReturn(
              okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create(transactionId = context.transactionId))))
            )
          )

        val getQuoteResponseString = invokeGetQuoteApi(context = context, cart = cart)
          .andExpect(status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val getQuoteResponse = verifyResponseMessage(
          getQuoteResponseString,
          ResponseMessage.ack(),
          expectedContext = context
        )
        verifyThatBppSelectApiWasInvoked(getQuoteResponse, cart, retailBengaluruBpp)
        verifier.verifyThatSubscriberLookupApiWasInvoked(registryBppLookupApi, retailBengaluruBpp)
      }
    }
  }

  private fun stubBppLookupApi(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi
      .stubFor(
        post("/lookup")
          .withRequestBody(matchingJsonPath("$.subscriber_id", equalTo(bppApi.baseUrl())))
          .willReturn(okJson(getSubscriberForBpp(bppApi)))
      )
  }

  private fun verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi: WireMockServer) =
    registryBppLookupApi.verify(0, postRequestedFor(urlEqualTo("/lookup")))

  private fun getSubscriberForBpp(bppApi: WireMockServer) =
    objectMapper.writeValueAsString(
      listOf(
        SubscriberDtoFactory.getDefault(
          subscriber_id = bppApi.baseUrl(),
          baseUrl = bppApi.baseUrl(),
          type = SubscriberDto.Type.BPP,
        )
      )
    )

  private fun verifyResponseMessage(
    getQuoteResponseString: String,
    expectedMessage: ResponseMessage,
    expectedError: ProtocolError? = null,
    expectedContext: ClientContext,
  ): ProtocolAckResponse {
    val getQuoteResponse = objectMapper.readValue(getQuoteResponseString, ProtocolAckResponse::class.java)
    getQuoteResponse.context shouldNotBe null
    getQuoteResponse.context?.messageId shouldNotBe null
    getQuoteResponse.context?.transactionId shouldBe expectedContext.transactionId
    getQuoteResponse.context?.action shouldBe ProtocolContext.Action.SELECT
    getQuoteResponse.message shouldBe expectedMessage
    getQuoteResponse.error shouldBe expectedError
    return getQuoteResponse
  }

  private fun verifyThatBppSelectApiWasInvoked(
    getQuoteResponse: ProtocolAckResponse,
    cart: CartDto,
    bppApi: WireMockServer
  ) {
    val protocolSelectRequest = getProtocolSelectRequest(getQuoteResponse, cart)
    bppApi.verify(
      postRequestedFor(urlEqualTo("/select"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(protocolSelectRequest)))
    )
  }

  private fun verifyThatBppSelectApiWasNotInvoked(bppApi: WireMockServer) =
    bppApi.verify(0, postRequestedFor(urlEqualTo("/select")))

  private fun getProtocolSelectRequest(getQuoteResponse: ProtocolAckResponse, cart: CartDto): ProtocolSelectRequest {
    val locations = cart.items?.first()?.provider?.locations?.map { ProtocolLocation(id = it) }
    return ProtocolSelectRequest(
      context = getQuoteResponse.context!!,
      message = ProtocolSelectRequestMessage(
        order = ProtocolSelectMessageSelected(
          provider = ProtocolProvider(
            id = cart.items?.first()?.provider?.id,
            locations = locations
          ),
          items = cart.items?.map {
            ProtocolSelectedItem(
              id = it.id,
              quantity =
              ProtocolItemQuantityAllocated(
                count = it.quantity.count, measure = it.quantity.measure
              ),
            )
          },
        )
      ),
    )
  }

  private fun invokeGetQuoteApi(context: ClientContext, cart: CartDto) = mockMvc
    .perform(
      MockMvcRequestBuilders.post("/client/v1/get_quote")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .content(
          objectMapper.writeValueAsString(
            GetQuoteRequestDto(context = context, message = GetQuoteRequestMessageDto(cart = cart))
          )
        )
    )
}
