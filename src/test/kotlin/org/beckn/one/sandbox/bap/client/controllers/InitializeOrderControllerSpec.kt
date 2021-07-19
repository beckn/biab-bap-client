package org.beckn.one.sandbox.bap.client.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.dtos.OrderRequestDto
import org.beckn.one.sandbox.bap.client.external.domains.Subscriber
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberLookupRequest
import org.beckn.one.sandbox.bap.client.factories.OrderDtoFactory
import org.beckn.one.sandbox.bap.common.City
import org.beckn.one.sandbox.bap.common.Country
import org.beckn.one.sandbox.bap.common.Domain
import org.beckn.one.sandbox.bap.common.factories.ResponseFactory
import org.beckn.one.sandbox.bap.common.factories.SubscriberDtoFactory
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.protocol.schemas.*
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.litote.kmongo.eq
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
class InitializeOrderControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory,
  val uuidFactory: UuidFactory,
  val messageRepository: GenericRepository<MessageDao>
) : DescribeSpec() {
  init {
    describe("Initialize order with BPP") {
      val registryBppLookupApi = WireMockServer(4010)
      registryBppLookupApi.start()
      val providerApi = WireMockServer(4013)
      providerApi.start()
      val provider2Api = WireMockServer(4014)
      provider2Api.start()
      val orderRequest = OrderRequestDto(
        message = OrderDtoFactory.create(
          bpp1_id = providerApi.baseUrl(),
          provider1_id = "padma coffee works"
        ), context = contextFactory.create()
      )

      beforeEach {
        providerApi.resetAll()
        registryBppLookupApi.resetAll()
        stubBppLookupApi(registryBppLookupApi, providerApi)
        stubBppLookupApi(registryBppLookupApi, provider2Api)
      }

      it("should return error when BPP init call fails") {
        providerApi.stubFor(WireMock.post("/init").willReturn(WireMock.serverError()))

        val initializeOrderResponseString =
          invokeInitializeOrder(orderRequest).andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val initializeOrderResponse =
          verifyInitResponseMessage(
            initializeOrderResponseString,
            orderRequest,
            ResponseMessage.nack(),
            ProtocolError("BAP_011", "BPP returned error")
          )
        verifyThatMessageWasNotPersisted(initializeOrderResponse)
        verifyThatBppInitApiWasInvoked(initializeOrderResponse, orderRequest, providerApi)
        verifyThatSubscriberLookupApiWasInvoked(registryBppLookupApi, providerApi)
      }

      it("should validate that order contains items from only one bpp") {

        val orderRequestWithMultipleBppItems =
          OrderRequestDto(
            context = contextFactory.create(),
            message = OrderDtoFactory.create(
              null,
              bpp1_id = providerApi.baseUrl(),
              bpp2_id = provider2Api.baseUrl(),
              provider1_id = "padma coffee works"
            )
          )

        val initializeOrderResponseString = invokeInitializeOrder(orderRequestWithMultipleBppItems)
          .andExpect(MockMvcResultMatchers.status().is4xxClientError)
          .andReturn()
          .response.contentAsString

        val initializeOrderResponse =
          verifyInitResponseMessage(
            initializeOrderResponseString,
            orderRequestWithMultipleBppItems,
            ResponseMessage.nack(),
            ProtocolError("BAP_014", "More than one BPP's item(s) selected/initialized")
          )
        verifyThatMessageWasNotPersisted(initializeOrderResponse)
        verifyThatBppInitApiWasNotInvoked(providerApi)
        verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi)
        verifyThatSubscriberLookupApiWasNotInvoked(provider2Api)
      }

      it("should validate that order contains items from only one provider") {
        val orderRequestWithMultipleProviderItems = OrderRequestDto(
          context = contextFactory.create(),
          message = OrderDtoFactory.create(
            null,
            bpp1_id = providerApi.baseUrl(),
            provider1_id = "padma coffee works",
            provider2_id = "Venugopal store"
          )
        )

        val initializeOrderResponseString = invokeInitializeOrder(orderRequestWithMultipleProviderItems)
          .andExpect(MockMvcResultMatchers.status().is4xxClientError)
          .andReturn()
          .response.contentAsString

        val initializeOrderResponse =
          verifyInitResponseMessage(
            initializeOrderResponseString,
            orderRequestWithMultipleProviderItems,
            ResponseMessage.nack(),
            ProtocolError("BAP_010", "More than one Provider's item(s) selected/initialized")
          )
        verifyThatMessageWasNotPersisted(initializeOrderResponse)
        verifyThatBppInitApiWasNotInvoked(providerApi)
        verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi)
        verifyThatSubscriberLookupApiWasNotInvoked(provider2Api)
      }

      it("should return null when cart items are empty") {
        val initRequestForTest = OrderRequestDto(
          message = OrderDtoFactory.create(
            bpp1_id = providerApi.baseUrl(),
            provider1_id = "padma coffee works",
            items = emptyList()
          ), context = contextFactory.create()
        )
        providerApi
          .stubFor(
            WireMock.post("/init").willReturn(
              WireMock.okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create())))
            )
          )

        val initializeOrderResponseString = invokeInitializeOrder(initRequestForTest)
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val confirmOrderResponse =
          verifyInitResponseMessage(initializeOrderResponseString, initRequestForTest, ResponseMessage.ack())
        verifyThatMessageWasNotPersisted(confirmOrderResponse)
        verifyThatBppInitApiWasNotInvoked(providerApi)
        verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi)
      }

      it("should invoke provide init api and save message") {
        providerApi
          .stubFor(
            WireMock.post("/init").willReturn(
              WireMock.okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create())))
            )
          )

        val initializeOrderResponseString = invokeInitializeOrder(orderRequest)
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val initializeOrderResponse =
          verifyInitResponseMessage(initializeOrderResponseString, orderRequest, ResponseMessage.ack())
        verifyThatMessageWasPersisted(initializeOrderResponse)
        verifyThatBppInitApiWasInvoked(initializeOrderResponse, orderRequest, providerApi)
        verifyThatSubscriberLookupApiWasInvoked(registryBppLookupApi, providerApi)
      }

      registryBppLookupApi.stop()

    }
  }

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

  private fun verifyThatBppInitApiWasNotInvoked(bppApi: WireMockServer) =
    bppApi.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/init")))

  private fun verifyThatSubscriberLookupApiWasNotInvoked(registryBppLookupApi: WireMockServer) =
    registryBppLookupApi.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/lookup")))

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

  private fun verifyInitResponseMessage(
      initializeOrderResponseString: String,
      orderRequest: OrderRequestDto,
      expectedMessage: ResponseMessage,
      expectedError: ProtocolError? = null
  ): ProtocolAckResponse {
    val initOrderResponse = objectMapper.readValue(initializeOrderResponseString, ProtocolAckResponse::class.java)
    initOrderResponse.context shouldNotBe null
    initOrderResponse.context?.messageId shouldNotBe null
    initOrderResponse.context?.transactionId shouldBe orderRequest.context.transactionId
    initOrderResponse.context?.action shouldBe ProtocolContext.Action.INIT
    initOrderResponse.message shouldBe expectedMessage
    initOrderResponse.error shouldBe expectedError
    return initOrderResponse
  }

  private fun verifyThatBppInitApiWasInvoked(
      initializeOrderResponse: ProtocolAckResponse,
      orderRequest: OrderRequestDto,
      providerApi: WireMockServer
  ) {
    val protocolInitRequest = getProtocolInitRequest(initializeOrderResponse, orderRequest)
    providerApi.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/init"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolInitRequest)))
    )
  }

  private fun getProtocolInitRequest(
      initializeOrderResponse: ProtocolAckResponse,
      orderRequest: OrderRequestDto
  ): ProtocolInitRequest {
    val locations =
      orderRequest.message.items?.first()?.provider?.locations?.map { ProtocolSelectMessageSelectedProviderLocations(id = it) }
    val provider =
      orderRequest.message.items?.first()?.provider// todo: does this hold good even for order object or is this gotten from somewhere else?
    return ProtocolInitRequest(
      context = initializeOrderResponse.context!!,
      message = ProtocolInitRequestMessage(
        order = ProtocolOrder(
          provider = ProtocolSelectMessageSelectedProvider(
            id = provider!!.id,
            locations = locations
          ),
          items = orderRequest.message.items!!.map {
            ProtocolSelectMessageSelectedItems(
              id = it.id,
              quantity = it.quantity
            )
          },
          billing = orderRequest.message.billingInfo,
          fulfillment = ProtocolFulfillment(
            end = ProtocolFulfillmentEnd(
              contact = ProtocolContact(
                phone = orderRequest.message.deliveryInfo.phone,
                email = orderRequest.message.deliveryInfo.email
              ), location = orderRequest.message.deliveryInfo.location
            ),
            type = "home_delivery",
            customer = ProtocolCustomer(person = ProtocolPerson(name = orderRequest.message.deliveryInfo.name))
          ),
          addOns = emptyList(),
          offers = emptyList()
        )
      )
    )
  }

  private fun verifyThatMessageWasPersisted(initializeOrderResponse: ProtocolAckResponse) {
    val savedMessage = messageRepository.findOne(MessageDao::id eq initializeOrderResponse.context?.messageId)
    savedMessage shouldNotBe null
  }

  private fun verifyThatMessageWasNotPersisted(initializeOrderResponse: ProtocolAckResponse) {
    val savedMessage = messageRepository.findOne(MessageDao::id eq initializeOrderResponse.context?.messageId)
    savedMessage shouldBe null
  }

  private fun invokeInitializeOrder(orderRequest: OrderRequestDto) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/initialize_order").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(orderRequest))
  )
}