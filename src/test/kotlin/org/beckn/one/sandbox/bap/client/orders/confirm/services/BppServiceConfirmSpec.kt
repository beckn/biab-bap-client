package org.beckn.one.sandbox.bap.client.orders.confirm.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.factories.DeliveryDtoFactory
import org.beckn.one.sandbox.bap.client.factories.OrderItemDtoFactory
import org.beckn.one.sandbox.bap.client.factories.OrderPaymentFactory
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.message.factories.*
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceConfirmSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory)
  private val bppServiceClient: BppClient = mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Confirm") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val confirmRequest = getConfirmRequest()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp confirm call fails with an exception") {
        `when`(bppServiceClient.confirm(getConfirmRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppConfirm()

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).confirm(getConfirmRequest())
      }

      it("should return bpp internal server error when bpp confirm call returns null body") {
        `when`(bppServiceClient.confirm(confirmRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppConfirm()

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).confirm(getConfirmRequest())
      }

      it("should return bpp internal server error when bpp confirm call returns nack response body") {
        val context = contextFactory.create()
        `when`(bppServiceClient.confirm(confirmRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppConfirm()

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).confirm(getConfirmRequest())
      }
    }
  }

  private fun invokeBppConfirm(): Either<BppError, ProtocolAckResponse> {
    return bppService.confirm(
      context = contextFactory.create(),
      bppUri = bppUri,
      order = OrderDto(
        deliveryInfo = DeliveryDtoFactory.create(),
        billingInfo = ProtocolBillingFactory.create(),
        items = listOf(OrderItemDtoFactory.create(bppUri, "padma coffee works", "123")),
        payment = OrderPaymentFactory.create()
      )
    )
  }

  private fun getConfirmRequest() = ProtocolConfirmRequest(
    context = contextFactory.create(),
    message = ProtocolConfirmRequestMessage(
      order = ProtocolOrder(
        provider = ProtocolSelectMessageSelectedProvider(
          id = "padma coffee works",
          locations = listOf(ProtocolSelectMessageSelectedProviderLocations("13.001581,77.5703686"))
        ),
        items = listOf(
          OrderItemDtoFactory.create(
            bppUri,
            "padma coffee works",
            "123"
          )
        ).map { ProtocolSelectMessageSelectedItems(id = it.id, quantity = it.quantity) },
        billing = ProtocolBillingFactory.create(),
        fulfillment = ProtocolFulfillment(
          end = ProtocolFulfillmentEnd(
            contact = ProtocolContact(
              phone = "9999999999",
              email = "test@gmail.com"
            ), location = ProtocolLocation(
              address = ProtocolAddress(
                door = "A",
                country = "IND",
                city = "std:080",
                street = "Bannerghatta Road",
                areaCode = "560076",
                state = "KA",
                building = "Pine Apartments"
              ),
              gps = "12,77"
            )
          ),
          type = "home_delivery",
          customer = ProtocolCustomer(person = ProtocolPerson(name = "Test"))
        ),
        addOns = emptyList(),
        offers = emptyList(),
        payment = ProtocolPayment(
          params = mapOf("amount" to (23.3).toString()),
          status = ProtocolPayment.Status.PAID
        )
      )
    )
  )
}
