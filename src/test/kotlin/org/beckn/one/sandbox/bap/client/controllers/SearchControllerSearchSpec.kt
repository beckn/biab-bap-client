package org.beckn.one.sandbox.bap.client.discovery.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.client.factories.SearchRequestFactory
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.registry
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.registryBppLookupApi
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.retailBengaluruBg
import org.beckn.one.sandbox.bap.common.factories.MockNetwork.retailBengaluruBpp
import org.beckn.one.sandbox.bap.common.factories.ResponseFactory
import org.beckn.one.sandbox.bap.common.factories.SubscriberDtoFactory
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ResponseStatus.ACK
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.litote.kmongo.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class SearchControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory,
  val messageRepository: GenericRepository<MessageDao>
) : DescribeSpec() {
  init {

    describe("Search") {
      MockNetwork.startAllSubscribers()
      beforeEach {
        MockNetwork.resetAllSubscribers()
      }

      it("should return error response when registry lookup fails") {
        registry
          .stubFor(post("/lookup").willReturn(serverError()))

        invokeSearchApi()
          .andExpect(status().is5xxServerError)
          .andExpect(jsonPath("$.message.ack.status", `is`("NACK")))
          .andExpect(jsonPath("$.error.code", `is`("BAP_001")))
          .andExpect(jsonPath("$.error.message", `is`("Registry lookup returned error")))
      }

      it("should return error response when registry lookup fails with location") {
        registry
          .stubFor(post("/lookup").willReturn(serverError()))

        invokeSearchApi(location = "40.741895,-73.989308")
          .andExpect(status().is5xxServerError)
          .andExpect(jsonPath("$.message.ack.status", `is`("NACK")))
          .andExpect(jsonPath("$.error.code", `is`("BAP_001")))
          .andExpect(jsonPath("$.error.message", `is`("Registry lookup returned error")))
      }

      it("should invoke Beckn /search API on first gateway and persist message") {
        stubLookupApi()
        stubSearchApi()

        val result: MvcResult = invokeSearchApi()
          .andExpect(status().is2xxSuccessful)
          .andExpect(jsonPath("$.message.ack.status", `is`(ACK.status)))
          .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
          .andReturn()

        verifyThatSearchApiWasInvoked()
        verifyThatSearchMessageWasPersisted(result)
      }

      it("should invoke Beckn /search API on first gateway and persist message with location") {
        stubLookupApi()
        stubSearchApi()

        val result: MvcResult = invokeSearchApi(location = "40.741895,-73.989308")
          .andExpect(status().is2xxSuccessful)
          .andExpect(jsonPath("$.message.ack.status", `is`(ACK.status)))
          .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
          .andReturn()

        verifyThatSearchApiWasInvoked()
        verifyThatSearchMessageWasPersisted(result)
      }

      it("should invoke Beckn /search API on specified BPP using gateway and persist message with location") {
        val bpp = MockNetwork.getRetailBengaluruBpp()
        stubBppSearchApi()
        stubBppLookupApi(registryBppLookupApi, retailBengaluruBpp, bpp.subscriber_id)

        val result: MvcResult =
          invokeSearchApi(location = "12.9259,77.583", providerId = "tulsidev", bppId = bpp.subscriber_id)
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.message.ack.status", `is`(ACK.status)))
            .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
            .andReturn()

        val searchResponse = verifyThatSearchMessageWasPersisted(result)
        verifyThatBppSearchWasInvoked(searchResponse, "tulsidev", "12.9259,77.583")
      }
    }
  }

  private fun verifyThatBppSearchWasInvoked(
    searchResponse: ProtocolAckResponse,
    providerId: String,
    providerLocation: String
  ) {
    val protocolSearchRequest = SearchRequestFactory.create(searchResponse.context!!, providerId, providerLocation)
    retailBengaluruBpp.verify(
      postRequestedFor(urlEqualTo("/search"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(protocolSearchRequest)))
    )
  }

  private fun stubBppSearchApi() {
    retailBengaluruBpp
      .stubFor(
        post("/search")
          .willReturn(okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create()))))
      )
  }

  private fun stubBppLookupApi(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer,
    bppId: String,
  ) {
    registryBppLookupApi
      .stubFor(
        post("/lookup")
          .withRequestBody(matchingJsonPath("$.subscriber_id", equalTo(bppId)))
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

  private fun verifyThatSearchMessageWasPersisted(result: MvcResult): ProtocolAckResponse {
    val searchResponse = objectMapper.readValue(result.response.contentAsString, ProtocolAckResponse::class.java)
    val savedMessage = messageRepository.findOne(MessageDao::id eq searchResponse.context?.messageId)
    savedMessage shouldNotBe null
    savedMessage?.id shouldBe searchResponse.context?.messageId
    savedMessage?.type shouldBe MessageDao.Type.Search
    return searchResponse
  }

  private fun verifyThatSearchApiWasInvoked() {
    retailBengaluruBg.verify(postRequestedFor(urlEqualTo("/search")))
  }

  private fun stubLookupApi() {
    val gatewaysJson = objectMapper.writeValueAsString(MockNetwork.getAllGateways())
    registry
      .stubFor(post("/lookup").willReturn(okJson(gatewaysJson)))
  }

  private fun stubSearchApi() {
    retailBengaluruBg
      .stubFor(
        post("/search").willReturn(
          okJson(
            objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create()))
          )
        )
      )
  }

  private fun invokeSearchApi(location: String = "", providerId: String = "", bppId: String = "") = mockMvc
    .perform(
      get("/client/v1/search")
        .param("searchString", "Fictional mystery books")
        .param("location", location)
        .param("bppId", bppId)
        .param("providerId", providerId)
    )
}
