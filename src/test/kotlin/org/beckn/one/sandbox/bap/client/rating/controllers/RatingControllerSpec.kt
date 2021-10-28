package org.beckn.one.sandbox.bap.client.rating.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.client.external.domains.Subscriber
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberLookupRequest
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.shared.dtos.RatingRequestDto
import org.beckn.one.sandbox.bap.client.shared.dtos.RatingRequestMessage
import org.beckn.one.sandbox.bap.common.City
import org.beckn.one.sandbox.bap.common.Country
import org.beckn.one.sandbox.bap.common.Domain
import org.beckn.one.sandbox.bap.common.Verifier
import org.beckn.one.sandbox.bap.common.factories.MockNetwork
import org.beckn.one.sandbox.bap.common.factories.ResponseFactory
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
class RatingControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory,
    val uuidFactory: UuidFactory,
) : DescribeSpec() {
  val verifier: Verifier = Verifier(objectMapper)

  init {

    describe("Rating an item") {
      MockNetwork.startAllSubscribers()
      val context =
        ClientContext(transactionId = uuidFactory.create(), bppId = MockNetwork.retailBengaluruBpp.baseUrl())
      val ratingRequest = RatingRequestDto(
        context = context,
        message = RatingRequestMessage(
          refId = "abc123",
          value = 4
        ),
      )

      beforeEach {
        MockNetwork.resetAllSubscribers()
        MockNetwork.stubBppLookupApi(MockNetwork.retailBengaluruBpp, objectMapper)
      }

      it("should return error when BPP rating call fails") {
        MockNetwork.retailBengaluruBpp.stubFor(post("/rating").willReturn(serverError()))

        val ratingResponseString =
          invokeRatingApi(ratingRequest)
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andReturn().response.contentAsString

        val ratingResponse =
          verifier.verifyResponseMessage(
            ratingResponseString,
            ResponseMessage.nack(),
            ProtocolError("BAP_011", "BPP returned error"),
            ProtocolContext.Action.RATING
          )
        ratingResponse.context?.transactionId shouldBe ratingRequest.context.transactionId
        verifyThatBppRatingApiWasInvoked(ratingResponse, ratingRequest, MockNetwork.retailBengaluruBpp)
        verifier.verifyThatSubscriberLookupApiWasInvoked(
          MockNetwork.registryBppLookupApi,
          MockNetwork.retailBengaluruBpp
        )
      }

      it("should invoke provide rating api") {
        MockNetwork.retailBengaluruBpp
          .stubFor(
            post("/rating").willReturn(
              okJson(objectMapper.writeValueAsString(ResponseFactory.getDefault(contextFactory.create())))
            )
          )

        val ratingResponseString = invokeRatingApi(ratingRequest)
          .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
          .andReturn()
          .response.contentAsString

        val ratingResponse =
          verifier.verifyResponseMessage(
            ratingResponseString,
            ResponseMessage.ack(),
            null,
            ProtocolContext.Action.RATING
          )
        verifyThatBppRatingApiWasInvoked(ratingResponse, ratingRequest, MockNetwork.retailBengaluruBpp)
        verifyThatSubscriberLookupApiWasInvoked(MockNetwork.registryBppLookupApi, MockNetwork.retailBengaluruBpp)
      }
    }
  }

  private fun invokeRatingApi(ratingRequest: RatingRequestDto) = mockMvc.perform(
    MockMvcRequestBuilders.post("/client/v1/rating").header(
      org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
    ).content(objectMapper.writeValueAsString(ratingRequest))
  )

  private fun verifyThatSubscriberLookupApiWasInvoked(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi.verify(
      postRequestedFor(urlEqualTo("/lookup"))
        .withRequestBody(
          equalToJson(
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

  private fun verifyThatBppRatingApiWasInvoked(
    ratingResponse: ProtocolAckResponse,
    ratingRequest: RatingRequestDto,
    providerApi: WireMockServer
  ) {
    val protocolRatingRequest = getProtocolRatingRequest(ratingResponse, ratingRequest)
    providerApi.verify(
      postRequestedFor(urlEqualTo("/rating"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(protocolRatingRequest)))
    )
  }

  private fun getProtocolRatingRequest(
    ratingResponse: ProtocolAckResponse,
    ratingRequest: RatingRequestDto
  ): ProtocolRatingRequest = ProtocolRatingRequest(
    context = ratingResponse.context!!,
    message = ProtocolRatingRequestMessage(
      id = ratingRequest.message.refId,
      value = ratingRequest.message.value
    )
  )

}