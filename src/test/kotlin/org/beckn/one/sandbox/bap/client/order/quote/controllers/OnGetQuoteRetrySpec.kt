package org.beckn.one.sandbox.bap.client.order.quote.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.client.external.toJson
import org.beckn.one.sandbox.bap.common.factories.MockProtocolBap
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolOnSelectMessageSelectedFactory
import org.beckn.protocol.schemas.ProtocolOnSelect
import org.beckn.protocol.schemas.ProtocolOnSelectMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnGetQuoteRetrySpec @Autowired constructor(
  contextFactory: ContextFactory,
  private val objectMapper: ObjectMapper,
  private val controller: OnGetQuotePollController,
) : DescribeSpec() {

  val context = contextFactory.create()
  private val protocolOnSelect = ProtocolOnSelect(
    context,
    message = ProtocolOnSelectMessage(order = ProtocolOnSelectMessageSelectedFactory.create())
  )
  val mockProtocolBap = MockProtocolBap.withResetInstance()

  init {
    describe("OnGetQuote callback") {

      context("when called for given message id") {

        it("should retry bpp select call if api returns error") {
          val protocolOnSelect = ProtocolOnSelect(
            context,
            message = ProtocolOnSelectMessage(order = ProtocolOnSelectMessageSelectedFactory.create())
          )
          stubBapOnSelectApi(response = serverError(), forState = Scenario.STARTED, nextState = "Success")
          stubBapOnSelectApi(response = okJson(objectMapper.toJson(listOf(protocolOnSelect))), forState = "Success")

          val results = controller.onGetQuoteV1(context.messageId)

          results.statusCode shouldBe HttpStatus.OK
          verifyBapOnSelectApiIsInvoked(2)
        }

        it("should fail after max retry attempts") {
          stubBapOnSelectApi(response = serverError(), forState = Scenario.STARTED, nextState = "Failure")
          stubBapOnSelectApi(response = serverError(), forState = "Failure", nextState = "Failure")

          val results = controller.onGetQuoteV1(context.messageId)

          results.statusCode shouldBe HttpStatus.OK
          verifyBapOnSelectApiIsInvoked(3)
        }
      }
    }
  }

  private fun stubBapOnSelectApi(
    nextState: String = "Success",
    forState: String = Scenario.STARTED,
    response: ResponseDefinitionBuilder?
  ) {
    mockProtocolBap
      .stubFor(
        get("/protocol/response/v1/on_select?messageId=${context.messageId}")
          .inScenario("Retry Scenario")
          .whenScenarioStateIs(forState)
          .willReturn(response)
          .willSetStateTo(nextState)
      )
  }

  private fun verifyBapOnSelectApiIsInvoked(numberOfTimes: Int = 1) {
    mockProtocolBap.verify(
      numberOfTimes,
      getRequestedFor(urlEqualTo("/protocol/response/v1/on_select?messageId=${context.messageId}"))
    )
  }

}