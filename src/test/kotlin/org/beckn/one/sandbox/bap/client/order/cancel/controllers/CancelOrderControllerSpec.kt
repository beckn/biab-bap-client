package org.beckn.one.sandbox.bap.client.order.cancel.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
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
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
class CancelOrderControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory,
    val uuidFactory: UuidFactory
) : DescribeSpec() {
  init {
    describe("Cancel order") {
      MockNetwork.startAllSubscribers()

      val context =
        ClientContext(transactionId = uuidFactory.create(), bppId = MockNetwork.retailBengaluruBpp.baseUrl())
      val cancelOrderDto = CancelOrderDto(
        context = context,
        message = CancelOrderRequestMessage(orderId = "abc", cancellationReasonId = "1")
      )
      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }
      it("should return error when BPP cancel call fails") {
        MockNetwork.retailBengaluruBpp.stubFor(WireMock.post("/cancel").willReturn(WireMock.serverError()))

        val cancelOrderResponseString =
          invokeCancelOrder(cancelOrderDto).andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val cancelOrderResponse =
          verifyCancelResponseMessage(
            cancelOrderResponseString,
            cancelOrderDto,
            ResponseMessage.nack(),
            ProtocolError("BAP_011", "BPP returned error")
          )
        verifyThatBppCancelApiWasInvoked(cancelOrderResponse, cancelOrderDto, MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should return error when BPP cancel call is made without a BPP ID") {
        MockNetwork.retailBengaluruBpp.stubFor(WireMock.post("/cancel").willReturn(WireMock.serverError()))

        val cancelOrderDtoWithoutBPPId = CancelOrderDto(
          context = ClientContext(transactionId = uuidFactory.create()),
          message = CancelOrderRequestMessage(orderId = "abc", cancellationReasonId = "1")
        )

        val cancelOrderResponseString =
          invokeCancelOrder(cancelOrderDtoWithoutBPPId).andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andReturn().response.contentAsString

        verifyCancelResponseMessage(
          cancelOrderResponseString,
          cancelOrderDtoWithoutBPPId,
          ResponseMessage.nack(),
          ProtocolError("BAP_016", "BPP Id is mandatory")
        )
        verifyThatBppCancelApiWasNotInvoked(MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasNotInvoked(MockNetwork.registryBppLookupApi)
      }

      it("should invoke BPP cancel api and save message") {
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/cancel").willReturn(
            WireMock.okJson(
              objectMapper.writeValueAsString(
                ResponseFactory.getDefault(contextFactory.create())
              )
            )
          )
        )

        val cancelOrderResponseString =
          invokeCancelOrder(cancelOrderDto).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val cancelOrderResponse =
          verifyCancelResponseMessage(cancelOrderResponseString, cancelOrderDto, ResponseMessage.ack())
        verifyThatBppCancelApiWasInvoked(cancelOrderResponse, cancelOrderDto, MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      MockNetwork.registryBppLookupApi.stop()

    }
  }

  private fun verifyThatBppCancelApiWasNotInvoked(bppApi: WireMockServer) {
    bppApi.verify(0, postRequestedFor(urlEqualTo("/cancel")))
  }

  private fun verifyThatBppCancelApiWasInvoked(
    cancelOrderResponse: ProtocolAckResponse,
    cancelOrderDto: CancelOrderDto,
    providerApi: WireMockServer
  ) {
    val protocolCancelRequest = getProtocolCancelRequest(cancelOrderResponse, cancelOrderDto)
    providerApi.verify(
      postRequestedFor(urlEqualTo("/cancel"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolCancelRequest)))
    )
  }

  private fun verifyThatBppGetCancellationReasonsApiWasInvoked(
    orderPolicyResponse: ClientOrderPolicyResponse,
    orderPolicyDto: GetOrderPolicyDto,
    providerApi: WireMockServer
  ) {
    val protocolGetCancellationReasonsRequest = getProtocolGetCancellationReasonsRequest(orderPolicyDto)
    providerApi.verify(
      WireMock.postRequestedFor(urlEqualTo("/get_cancellation_reasons"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolGetCancellationReasonsRequest)))
    )
  }

  private fun getProtocolCancelRequest(
    cancelOrderResponse: ProtocolAckResponse,
    cancelOrderDto: CancelOrderDto
  ): ProtocolCancelRequest {
    return ProtocolCancelRequest(
      context = cancelOrderResponse.context!!,
      message = ProtocolCancelRequestMessage(
        orderId = cancelOrderDto.message.orderId,
        cancellationReasonId = cancelOrderDto.message.cancellationReasonId
      )
    )
  }

  private fun getProtocolGetCancellationReasonsRequest(
    orderPolicyDto: GetOrderPolicyDto
  ): ProtocolGetPolicyRequest =
    ProtocolGetPolicyRequest(
      context = getContext(
        orderPolicyDto.context.transactionId,
        orderPolicyDto.context.bppId
      )
    )

  private fun verifyThatSubscriberLookupApiWasInvoked(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi.verify(
      postRequestedFor(urlEqualTo("/lookup"))
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

  private fun verifyGetOrderPolicyResponseMessage(
    getOrderPolicyResponseString: String,
    getOrderPolicyDto: GetOrderPolicyDto,
    expectedMessage: ClientOrderPolicyResponse,
    expectedError: ProtocolError? = null
  ): ClientOrderPolicyResponse {
    val getOrderPolicyResponse =
      objectMapper.readValue(getOrderPolicyResponseString, ClientOrderPolicyResponse::class.java)
    getOrderPolicyResponse.context shouldNotBe null
    getOrderPolicyResponse.context.messageId shouldNotBe null
    getOrderPolicyResponse.context.transactionId shouldBe getOrderPolicyDto.context.transactionId
    getOrderPolicyResponse.context.action shouldBe ProtocolContext.Action.CANCEL
    getOrderPolicyResponse.message shouldBe expectedMessage
    getOrderPolicyResponse.error shouldBe expectedError
    return getOrderPolicyResponse
  }

  private fun verifyCancelResponseMessage(
    cancelOrderResponseString: String,
    cancelOrderDto: CancelOrderDto,
    expectedMessage: ResponseMessage,
    expectedError: ProtocolError? = null
  ): ProtocolAckResponse {
    val cancelOrderResponse = objectMapper.readValue(cancelOrderResponseString, ProtocolAckResponse::class.java)
    cancelOrderResponse.context shouldNotBe null
    cancelOrderResponse.context?.messageId shouldNotBe null
    cancelOrderResponse.context?.transactionId shouldBe cancelOrderDto.context.transactionId
    cancelOrderResponse.context?.action shouldBe ProtocolContext.Action.CANCEL
    cancelOrderResponse.message shouldBe expectedMessage
    cancelOrderResponse.error shouldBe expectedError
    return cancelOrderResponse
  }

  private fun invokeGetOrderPolicy(getOrderPolicyDto: GetOrderPolicyDto) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/get_cancellation_policy").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(getOrderPolicyDto))
  )

  private fun invokeCancelOrder(cancelOrderDto: CancelOrderDto) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/cancel_order").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(cancelOrderDto))
  )

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

  private fun getContext(transactionId: String, bppId: String?) =
    contextFactory.create(action = ProtocolContext.Action.CANCEL, transactionId = transactionId, bppId = bppId)

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

  private fun verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi: WireMockServer) =
    registryBppLookupApi.verify(0, WireMock.postRequestedFor(urlEqualTo("/lookup")))

}