package org.beckn.one.sandbox.bap.client.rating.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientRatingResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.factories.ProtocolOnRatingMessageFeedbackFactory
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnRating
import org.beckn.protocol.schemas.ProtocolOnRatingMessage
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnRatingPollControllerSpec @Autowired constructor(
    private val contextFactory: ContextFactory,
    private val mapper: ObjectMapper,
    private val protocolClient: ProtocolClient,
    private val mockMvc: MockMvc
) : DescribeSpec() {

  val context = contextFactory.create()
  private val protocolOnRating = ProtocolOnRating(
    context,
    message = ProtocolOnRatingMessage(feedback = ProtocolOnRatingMessageFeedbackFactory.create())
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnRating callback") {

      context("when called for given message id") {
        mockProtocolBap.stubFor(
          WireMock.get("/protocol/response/v1/on_rating?messageId=${context.messageId}")
            .willReturn(WireMock.okJson(mapper.writeValueAsString(ratingResults())))
        )
        val onRatingCall = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_rating")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", context.messageId)
          )

        it("should respond with status ok") {
          onRatingCall.andExpect(status().isOk)
        }

        it("should respond with all rating responses in body") {
          val results = onRatingCall.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientRatingResponse::class.java)
          clientResponse.message?.feedback shouldNotBe null
          clientResponse.message?.feedback shouldBe protocolOnRating.message?.feedback
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnRating, ClientRatingResponse>> {
          onGeneric { onPoll(any(), any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onRatingPollController = OnRatingPollController(mockOnPollService, contextFactory, protocolClient)
        it("should respond with failure") {
          val response = onRatingPollController.onRating(context.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun ratingResults(): List<ProtocolOnRating> {
    return listOf(
      protocolOnRating,
      protocolOnRating
    )
  }
}