package org.beckn.one.sandbox.bap.client.fulfillment.track.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.shared.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.common.Verifier
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.anotherRetailBengaluruBpp
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.registryBppLookupApi
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.retailBengaluruBpp
import org.beckn.one.sandbox.bap.common.factories.ResponseFactory
import org.beckn.one.sandbox.bap.common.factories.SubscriberDtoFactory
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class TrackControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory,
  val uuidFactory: UuidFactory,
) : DescribeSpec() {
  private val verifier = Verifier(objectMapper)

  init {
    describe("Track") {
      MockNetwork.startAllSubscribers()

      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(registryBppLookupApi, retailBengaluruBpp)
        stubBppLookupApi(registryBppLookupApi, anotherRetailBengaluruBpp)
      }

      it("should return error when bpp track call fails") {
        retailBengaluruBpp.stubFor(post("/track").willReturn(serverError()))

        val trackResponseString = invokeTrackApi(getTrackRequestDto())
          .andExpect(status().is5xxServerError)
          .andReturn().response.contentAsString

        val trackResponse =
          verifyResponseMessage(trackResponseString, ResponseMessage.nack(), BppError.Internal.error())
        verifyThatBppTrackApiWasInvoked(trackResponse, retailBengaluruBpp)
        verifier.verifyThatSubscriberLookupApiWasInvoked(
          registryBppLookupApi,
          retailBengaluruBpp
        )
      }

      it("should invoke provide track api and save message") {
        retailBengaluruBpp
          .stubFor(
            post("/track").willReturn(
              okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create())))
            )
          )

        val trackResponseString = invokeTrackApi(getTrackRequestDto())
          .andExpect(status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val trackResponse = verifyResponseMessage(
          responseString = trackResponseString,
          expectedMessage = ResponseMessage.ack(),
          expectedError = null
        )
        verifyThatBppTrackApiWasInvoked(trackResponse, retailBengaluruBpp)
        verifier.verifyThatSubscriberLookupApiWasInvoked(registryBppLookupApi, retailBengaluruBpp)
      }
    }
  }

  private fun invokeTrackApi(trackRequestDto: TrackRequestDto): ResultActions {
    return mockMvc
      .perform(
        MockMvcRequestBuilders.post("/client/v1/track")
          .content(objectMapper.writeValueAsString(trackRequestDto))
          .contentType(MediaType.APPLICATION_JSON)
      )
  }

  private fun getTrackRequestDto(bppId: String = retailBengaluruBpp.baseUrl()): TrackRequestDto {
    return TrackRequestDto(
      context = ClientContext(bppId = bppId, transactionId = uuidFactory.create()),
      message = ProtocolTrackRequestMessage(orderId = "order id 1"),
    )
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
    responseString: String,
    expectedMessage: ResponseMessage,
    expectedError: ProtocolError? = null
  ): ProtocolAckResponse {
    val getQuoteResponse = objectMapper.readValue(responseString, ProtocolAckResponse::class.java)
    getQuoteResponse.context shouldNotBe null
    getQuoteResponse.context?.messageId shouldNotBe null
    getQuoteResponse.context?.action shouldBe ProtocolContext.Action.TRACK
    getQuoteResponse.message shouldBe expectedMessage
    getQuoteResponse.error shouldBe expectedError
    return getQuoteResponse
  }

  private fun verifyThatBppTrackApiWasInvoked(
    trackResponse: ProtocolAckResponse,
    bppApi: WireMockServer
  ) {
    val protocolTrackRequest = getProtocolTrackRequest(trackResponse)
    bppApi.verify(
      postRequestedFor(urlEqualTo("/track"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(protocolTrackRequest)))
    )
  }

  private fun getProtocolTrackRequest(trackResponse: ProtocolAckResponse): ProtocolTrackRequest {
    return ProtocolTrackRequest(
      context = trackResponse.context!!,
      message = ProtocolTrackRequestMessage(orderId = "order id 1"),
    )
  }
}
