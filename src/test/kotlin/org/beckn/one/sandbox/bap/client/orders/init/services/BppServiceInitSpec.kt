package org.beckn.one.sandbox.bap.client.orders.init.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.factories.OrderDtoFactory
import org.beckn.one.sandbox.bap.client.factories.OrderItemDtoFactory
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.message.factories.ProtocolBillingFactory
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceInitSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory)
  private val bppServiceClient: BppClient = mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Init") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val initRequest = getInitRequest()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp init call fails with an exception") {
        `when`(bppServiceClient.init(getInitRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppInit()

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).init(getInitRequest())
      }

      it("should return bpp internal server error when bpp init call returns null body") {
        `when`(bppServiceClient.init(initRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppInit()

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).init(getInitRequest())
      }

      it("should return bpp internal server error when bpp init call returns nack response body") {
        val context = contextFactory.create()
        `when`(bppServiceClient.init(initRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppInit()

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).init(getInitRequest())
      }
    }
  }

  private fun invokeBppInit(): Either<BppError, ProtocolAckResponse> {
    return bppService.initialize(
      context = contextFactory.create(),
      bppUri = bppUri,
      order = OrderDtoFactory.create(
        bpp1_id = bppUri,
        provider1_id = "padma coffee works",
        items = listOf(
          OrderItemDtoFactory.create(
            bppUri,
            "padma coffee works",
            "123"
          )
        )
      )
    )
  }

  private fun getInitRequest() = ProtocolInitRequest(
    context = contextFactory.create(),
    message = ProtocolInitRequestMessage(
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
        offers = emptyList()
      )
    )
  )
}
