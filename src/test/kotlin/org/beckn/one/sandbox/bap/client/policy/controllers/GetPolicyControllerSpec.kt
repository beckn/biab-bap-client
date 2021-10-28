package org.beckn.one.sandbox.bap.client.policy.controllers

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
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.common.City
import org.beckn.one.sandbox.bap.common.Country
import org.beckn.one.sandbox.bap.common.Domain
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
import org.beckn.one.sandbox.bap.common.factories.SubscriberDtoFactory
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.factories.UuidFactory
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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class GetPolicyControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory,
  val uuidFactory: UuidFactory
) : DescribeSpec() {
  init {
    describe("Get order cancellation policies") {
      MockNetwork.startAllSubscribers()

      val context =
        ClientContext(transactionId = uuidFactory.create(), bppId = MockNetwork.retailBengaluruBpp.baseUrl())
      val getOrderPolicyDto = GetOrderPolicyDto(context = context)

      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("Should return error when BPP get cancellation reasons call fails") {
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_cancellation_reasons").willReturn(WireMock.serverError())
        )

        val getOrderPolicyResponseString =
          invokeGetPolicyApiCall(
            getOrderPolicyDto,
            "get_cancellation_policy"
          ).andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val getOrderPolicyResponse =
          verifyGetPolicyResponseMessage(
            getOrderPolicyResponseString,
            getOrderPolicyDto,
            null,
            BppError.Internal.error()
          )

        verifyThatBppGetPolicyApiWasInvoked(
          getOrderPolicyResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should invoke BPP get cancellation reasons api and save message") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            WireMock.post("/get_cancellation_reasons").willReturn(
              WireMock.okJson(
                objectMapper.writeValueAsString(
                  listOf(
                    ProtocolOption(
                      descriptor = ProtocolDescriptor(
                        name = "No Longer Required",
                        code = "2"
                      ),
                      id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
                    )
                  )
                )
              )
            )
          )

        val getOrderPolicyResponseString = invokeGetPolicyApiCall(getOrderPolicyDto, "get_cancellation_policy")
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val getOrderPolicyResponse = verifyGetPolicyResponseMessage(
          getOrderPolicyResponseString,
          getOrderPolicyDto,
          ClientOrderPolicyResponseMessage(
            cancellationReasons = listOf(
              ProtocolOption(
                descriptor = ProtocolDescriptor(
                  name = "No Longer Required",
                  code = "2"
                ),
                id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
              )
            )
          )
        )

        verifyThatBppGetPolicyApiWasInvoked(
          getOrderPolicyResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }
    }

    describe("Get rating categories") {

      val context =
        ClientContext(transactionId = uuidFactory.create(), bppId = MockNetwork.retailBengaluruBpp.baseUrl())
      val getOrderPolicyDto = GetOrderPolicyDto(context = context)

      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("Should return error when BPP get rating category call fails") {
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_rating_categories").willReturn(WireMock.serverError())
        )

        val getRatingCategoriesResponseString =
          invokeGetPolicyApiCall(
            getOrderPolicyDto,
            "get_rating_category"
          ).andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val getRatingCategoryResponse =
          verifyGetPolicyResponseMessage(
            getRatingCategoriesResponseString,
            getOrderPolicyDto,
            null,
            BppError.Internal.error()
          )

        verifyThatBppGetPolicyApiWasInvoked(
          getRatingCategoryResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should invoke BPP get rating category api and save message") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            WireMock.post("/get_rating_categories").willReturn(
              WireMock.okJson(
                objectMapper.writeValueAsString(
                  listOf(
                    ProtocolRatingCategory(
                      descriptor = ProtocolDescriptor(
                        name = "No Longer Required",
                        code = "2"
                      ),
                      question = "will this work?",
                      id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
                    )
                  )
                )
              )
            )
          )

        val getRatingCategoriesResponseString = invokeGetPolicyApiCall(getOrderPolicyDto, "get_rating_category")
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val getRatingCategoryResponse = verifyGetPolicyResponseMessage(
          getRatingCategoriesResponseString,
          getOrderPolicyDto,
          ClientOrderPolicyResponseMessage(
            ratingCategories = listOf(
              ProtocolRatingCategory(
                descriptor = ProtocolDescriptor(
                  name = "No Longer Required",
                  code = "2"
                ),
                question = "will this work?",
                id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
              )
            )
          )
        )

        verifyThatBppGetPolicyApiWasInvoked(
          getRatingCategoryResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }
    }

    describe("Get all order policies") {

      val context =
        ClientContext(transactionId = uuidFactory.create(), bppId = MockNetwork.retailBengaluruBpp.baseUrl())
      val getOrderPolicyDto = GetOrderPolicyDto(context = context)

      beforeEach {
        MockNetwork.resetAllSubscribers()
        stubBppLookupApi(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should return error code when both API return errors") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            WireMock.post("/get_rating_categories").willReturn(WireMock.serverError())
          )
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_cancellation_reasons").willReturn(WireMock.serverError())
        )

        val getPoliciesResponseString = invokeGetPolicyApiCall(getOrderPolicyDto, "get_order_policy")
          .andExpect(MockMvcResultMatchers.status().isInternalServerError)
          .andReturn()
          .response.contentAsString

        val getPoliciesResponse = verifyGetMultiplePolicyResponseMessage(
          getPoliciesResponseString,
          getOrderPolicyDto,
          null,
          mutableListOf(BppError.Internal.error(), BppError.Internal.error())
        )

        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("Should return 200 but error in body when BPP get rating category call fails but cancellation reasons succeeds") {
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_rating_categories").willReturn(WireMock.serverError())
        )
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_cancellation_reasons").willReturn(
            WireMock.okJson(
              objectMapper.writeValueAsString(
                listOf(
                  ProtocolOption(
                    descriptor = ProtocolDescriptor(
                      name = "No Longer Required",
                      code = "2"
                    ),
                    id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
                  )
                )
              )
            )
          )
        )

        val getPoliciesResponseString =
          invokeGetPolicyApiCall(
            getOrderPolicyDto,
            "get_order_policy"
          ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val getPoliciesResponse =
          verifyGetMultiplePolicyResponseMessage(
            getPoliciesResponseString,
            getOrderPolicyDto,
            ClientOrderPolicyResponseMessage(
              cancellationReasons = listOf(
                ProtocolOption(
                  descriptor = ProtocolDescriptor(
                    name = "No Longer Required",
                    code = "2"
                  ),
                  id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
                )
              )
            ),
            mutableListOf(BppError.Internal.error())
          )

        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("Should return 200 but error in body when BPP get cancellation reasons call fails but rating category succeeds") {
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_rating_categories").willReturn(
            WireMock.okJson(
              objectMapper.writeValueAsString(
                listOf(
                  ProtocolRatingCategory(
                    descriptor = ProtocolDescriptor(
                      name = "No Longer Required",
                      code = "2"
                    ),
                    question = "will this work?",
                    id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
                  )
                )
              )
            )
          )
        )
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_cancellation_reasons").willReturn(WireMock.serverError())
        )

        val getPoliciesResponseString =
          invokeGetPolicyApiCall(
            getOrderPolicyDto,
            "get_order_policy"
          ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val getPoliciesResponse =
          verifyGetMultiplePolicyResponseMessage(
            getPoliciesResponseString,
            getOrderPolicyDto,
            ClientOrderPolicyResponseMessage(
              ratingCategories = listOf(
                ProtocolRatingCategory(
                  descriptor = ProtocolDescriptor(
                    name = "No Longer Required",
                    code = "2"
                  ),
                  question = "will this work?",
                  id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
                )
              )
            ),
            mutableListOf(BppError.Internal.error())
          )

        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      it("should return 200 with neither errors when both succeed and then save message") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            WireMock.post("/get_rating_categories").willReturn(
              WireMock.okJson(
                objectMapper.writeValueAsString(
                  listOf(
                    ProtocolRatingCategory(
                      descriptor = ProtocolDescriptor(
                        name = "No Longer Required",
                        code = "2"
                      ),
                      question = "will this work?",
                      id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
                    )
                  )
                )
              )
            )
          )
        MockNetwork.retailBengaluruBpp.stubFor(
          WireMock.post("/get_cancellation_reasons").willReturn(
            WireMock.okJson(
              objectMapper.writeValueAsString(
                listOf(
                  ProtocolOption(
                    descriptor = ProtocolDescriptor(
                      name = "No Longer Required",
                      code = "2"
                    ),
                    id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
                  )
                )
              )
            )
          )
        )

        val getPoliciesResponseString = invokeGetPolicyApiCall(getOrderPolicyDto, "get_order_policy")
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val getPoliciesResponse = verifyGetMultiplePolicyResponseMessage(
          getPoliciesResponseString,
          getOrderPolicyDto,
          ClientOrderPolicyResponseMessage(
            ratingCategories = listOf(
              ProtocolRatingCategory(
                descriptor = ProtocolDescriptor(
                  name = "No Longer Required",
                  code = "2"
                ),
                question = "will this work?",
                id = ".retail.kiranaind.blr2@mandi.succinct.in.rating_categories"
              )
            ),
            cancellationReasons = listOf(
              ProtocolOption(
                descriptor = ProtocolDescriptor(
                  name = "No Longer Required",
                  code = "2"
                ),
                id = ".retail.kiranaind.blr2@mandi.succinct.in.cancellation_reason"
              )
            )
          ),
          mutableListOf()
        )

        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_cancellation_reasons"
        )
        verifyThatBppGetPolicyApiWasInvoked(
          getPoliciesResponse,
          MockNetwork.retailBengaluruBpp,
          "get_rating_categories"
        )
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }

      MockNetwork.registryBppLookupApi.stop()
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

  private fun verifyThatBppGetPolicyApiWasInvoked(
    orderPolicyResponse: ClientOrderPolicyResponse,
    providerApi: WireMockServer,
    apiEndpoint: String
  ) {
    val protocolGetRatingCategoriesRequest = getProtocolPolicyRequest(orderPolicyResponse)
    providerApi.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/$apiEndpoint"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolGetRatingCategoriesRequest)))
    )
  }

  private fun getProtocolPolicyRequest(
    orderPolicyResponse: ClientOrderPolicyResponse
  ): ProtocolGetPolicyRequest =
    ProtocolGetPolicyRequest(
      context = orderPolicyResponse.context
    )

  private fun verifyThatBppGetPolicyApiWasInvoked(
    orderPolicyResponse: ClientOrderPolicyMultipleResponse,
    providerApi: WireMockServer,
    apiEndpoint: String
  ) {
    val protocolGetRatingCategoriesRequest = getProtocolPolicyRequest(orderPolicyResponse)
    providerApi.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/$apiEndpoint"))
        .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(protocolGetRatingCategoriesRequest)))
    )
  }

  private fun getProtocolPolicyRequest(
    orderPolicyResponse: ClientOrderPolicyMultipleResponse
  ): ProtocolGetPolicyRequest =
    ProtocolGetPolicyRequest(
      context = orderPolicyResponse.context
    )

  private fun verifyGetPolicyResponseMessage(
    getOrderPolicyResponseString: String,
    getOrderPolicyDto: GetOrderPolicyDto,
    expectedMessage: ClientOrderPolicyResponseMessage?,
    expectedError: ProtocolError? = null
  ): ClientOrderPolicyResponse {
    val getOrderPolicyResponse =
      objectMapper.readValue(getOrderPolicyResponseString, ClientOrderPolicyResponse::class.java)
    getOrderPolicyResponse.context shouldNotBe null
    getOrderPolicyResponse.context.messageId shouldNotBe null
    getOrderPolicyResponse.context.transactionId shouldBe getOrderPolicyDto.context.transactionId
    getOrderPolicyResponse.context.action shouldBe ProtocolContext.Action.SEARCH
    getOrderPolicyResponse.message shouldBe expectedMessage
    getOrderPolicyResponse.error shouldBe expectedError
    return getOrderPolicyResponse
  }

  private fun verifyGetMultiplePolicyResponseMessage(
    getOrderPolicyResponseString: String,
    getOrderPolicyDto: GetOrderPolicyDto,
    expectedMessage: ClientOrderPolicyResponseMessage?,
    expectedErrors: MutableList<ProtocolError>? = mutableListOf()
  ): ClientOrderPolicyMultipleResponse {
    val getOrderPolicyResponse =
      objectMapper.readValue(getOrderPolicyResponseString, ClientOrderPolicyMultipleResponse::class.java)
    getOrderPolicyResponse.context shouldNotBe null
    getOrderPolicyResponse.context.messageId shouldNotBe null
    getOrderPolicyResponse.context.transactionId shouldBe getOrderPolicyDto.context.transactionId
    getOrderPolicyResponse.context.action shouldBe ProtocolContext.Action.SEARCH
    getOrderPolicyResponse.message shouldBe expectedMessage
    getOrderPolicyResponse.error shouldBe expectedErrors
    return getOrderPolicyResponse
  }

  private fun invokeGetPolicyApiCall(getOrderPolicyDto: GetOrderPolicyDto, apiEndpoint: String) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/$apiEndpoint").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(getOrderPolicyDto))
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

}