package org.beckn.one.sandbox.bap.client.order.policy.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientOrderPolicyResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientOrderPolicyResponseMessage
import org.beckn.one.sandbox.bap.client.shared.dtos.GetOrderPolicyDto
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolDescriptor
import org.beckn.protocol.schemas.ProtocolOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class MockGetOrderPolicyControllerSpec @Autowired constructor(
  val mockMvc: MockMvc,
  val objectMapper: ObjectMapper,
  val contextFactory: ContextFactory
) : DescribeSpec() {

  init {
    describe("Get Order Policy") {
      it("should return order policy") {
        val context = ClientContext()

        val getOrderPolicyResponseBody = invokeGetOrderPolicyApi(context)
          .andExpect(status().isOk)
          .andReturn()

        val getOrderPolicyResponse = objectMapper.readValue(
          getOrderPolicyResponseBody.response.contentAsString,
          ClientOrderPolicyResponse::class.java
        )
        getOrderPolicyResponse.message shouldBe ClientOrderPolicyResponseMessage(
          cancellationReasons = listOf(
            ProtocolOption("1", cancellationPolicy()),
            ProtocolOption("2", returnPolicy()),
          )
        )
      }
    }
  }

  private fun invokeGetOrderPolicyApi(context: ClientContext) = mockMvc
    .perform(
      post("/client/v0/get_order_policy")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .content(objectMapper.writeValueAsString(GetOrderPolicyDto(context = context)))
    )

  private fun cancellationPolicy() = ProtocolDescriptor(
    name = "Cancellation Policy",
    code = "Cancellable within a day",
    shortDesc = "This item is cancellable with a day of the order being placed.",
    longDesc = "However if there is a delay in delivery and you would like to cancel the order after a day of placing the order then please contact the customer support.",
  )

  private fun returnPolicy() = ProtocolDescriptor(
    name = "Return Policy",
    code = "Non-Returnable",
    shortDesc = "This item is non-returnable due to the consumable nature of the product.",
    longDesc = "However, in the unlikely event of damaged, defective or different/wrong item delivered to you, we will provide a full refund or free replacement as applicable. We may contact you to ascertain the damage or defect in the product prior to issuing refund/replacement.",
  )


}
