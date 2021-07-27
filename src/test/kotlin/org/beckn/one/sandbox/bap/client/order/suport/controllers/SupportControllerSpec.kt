package org.beckn.one.sandbox.bap.client.order.suport.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.domains.Subscriber
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberLookupRequest
import org.beckn.one.sandbox.bap.client.shared.dtos.*
import org.beckn.one.sandbox.bap.common.City
import org.beckn.one.sandbox.bap.common.Country
import org.beckn.one.sandbox.bap.common.Domain
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class SupportControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory,
  val uuidFactory: UuidFactory,
) : DescribeSpec()  {
  init {
    describe("Get support details from BPP") {
      MockNetwork.startAllSubscribers()
      val context = ClientContext(transactionId = uuidFactory.create())
      val supportRequest = SupportRequestDto(
        context = context,
        message = SupportRequestMessage(
          bppId = MockNetwork.retailBengaluruBpp.baseUrl(),
          refId = "abc123"
        ),
      )

      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should return error when BPP support call fails") {
        MockNetwork.retailBengaluruBpp.stubFor(WireMock.post("/support").willReturn(WireMock.serverError()))

        val supportResponseString =
          invokeSupportOrder(supportRequest).andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val supportResponse =
          verifySupportResponseMessage(
            supportResponseString,
            supportRequest,
            ResponseMessage.nack(),
            ProtocolError("BAP_011", "BPP returned error")
          )
        verifyThatBppSupportApiWasInvoked(supportResponse, supportRequest, MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should invoke provide init api and save message") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            WireMock.post("/support").willReturn(
              WireMock.okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create())))
            )
          )

        val supportResponseString = invokeSupportOrder(supportRequest)
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val supportResponse =
          verifySupportResponseMessage(supportResponseString, supportRequest, ResponseMessage.ack())
        verifyThatBppSupportApiWasInvoked(supportResponse, supportRequest, MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      MockNetwork.registryBppLookupApi.stop()

    }
  }

  private fun stubBppLookupApi(
    registryBppLookupApi: WireMockServer,
    providerApi: WireMockServer
  ) {
    registryBppLookupApi
      .stubFor(
        WireMock.post("/lookup")
          .withRequestBody(WireMock.matchingJsonPath("$.subscriber_id", WireMock.equalTo(providerApi.baseUrl())))
          .willReturn(WireMock.okJson(getSubscriberForBpp(providerApi)))
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

  private fun invokeSupportOrder(supportRequest: SupportRequestDto) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/get_support").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(supportRequest)).param("signature", "abc")
  )

  private fun verifyThatSubscriberLookupApiWasInvoked(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/lookup"))
        .withRequestBody(
          WireMock.equalToJson(
            objectMapper.writeValueAsString(
              SubscriberLookupRequest(
                subscriber_id = bppApi.baseUrl(),
                type = Subscriber.Type.BPP,
                domain = Domain.LocalRetail.value,
                country = Country.India.value,
                city = City.Bengaluru.value
              )
            )
          )
        )
    )
  }

  private fun verifySupportResponseMessage(
    supportResponseString: String,
    supportRequest: SupportRequestDto,
    expectedMessage: ResponseMessage,
    expectedError: ProtocolError? = null
  ): ProtocolAckResponse {
    val supportOrderResponse = objectMapper.readValue(supportResponseString, ProtocolAckResponse::class.java)
    supportOrderResponse.context shouldNotBe null
    supportOrderResponse.context?.messageId shouldNotBe null
    supportOrderResponse.context?.transactionId shouldBe supportRequest.context.transactionId
    supportOrderResponse.context?.action shouldBe ProtocolContext.Action.SUPPORT
    supportOrderResponse.message shouldBe expectedMessage
    supportOrderResponse.error shouldBe expectedError
    return supportOrderResponse
  }

  private fun verifyThatBppSupportApiWasInvoked(
    supportResponse: ProtocolAckResponse,
    supportRequest: SupportRequestDto,
    providerApi: WireMockServer
  ) {
    val protocolSupportRequest = getProtocolSupportRequest(supportResponse, supportRequest)
    providerApi.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/support"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolSupportRequest)))
    )
  }

  private fun getProtocolSupportRequest(
    supportResponse: ProtocolAckResponse,
    supportRequest: SupportRequestDto
  ): ProtocolSupportRequest = ProtocolSupportRequest(
    context = supportResponse.context!!,
    message = ProtocolSupportRequestMessage(
      refId = supportRequest.message.refId
    )
  )

}