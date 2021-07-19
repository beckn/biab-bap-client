package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.dtos.OrderPayment
import org.beckn.one.sandbox.bap.client.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.external.provider.BppServiceClient
import org.beckn.one.sandbox.bap.client.factories.DeliveryDtoFactory
import org.beckn.one.sandbox.bap.client.factories.OrderItemDtoFactory
import org.beckn.one.sandbox.bap.client.factories.SearchRequestFactory
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.message.factories.IdFactory
import org.beckn.one.sandbox.bap.message.factories.*
import org.beckn.one.sandbox.bap.schemas.*
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppServiceClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory)
  private val bppServiceClient: BppServiceClient = mock(BppServiceClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Search") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val context = contextFactory.create()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp search call fails with an exception") {
        val criteria = SearchCriteria(providerId = "venugopala stores", location = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }

      it("should return bpp internal server error when bpp search call returns null body") {
        val criteria = SearchCriteria(providerId = "venugopala stores", location = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.response(null)
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }

      it("should return bpp internal server error when bpp search call returns nack response body") {
        val criteria = SearchCriteria(providerId = "venugopala stores", location = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }
    }

    describe("Select") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val selectRequest = getSelectRequest()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp select call fails with an exception") {
        `when`(bppServiceClient.select(getSelectRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).select(getSelectRequest())
      }

      it("should return bpp internal server error when bpp select call returns null body") {
        `when`(bppServiceClient.select(selectRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).select(getSelectRequest())
      }

      it("should return bpp internal server error when bpp select call returns nack response body") {
        val context = contextFactory.create()
        `when`(bppServiceClient.select(selectRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).select(getSelectRequest())
      }
    }

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

  private fun getSearchRequest(
      context: ProtocolContext,
      criteria: SearchCriteria
  ) = SearchRequestFactory.create(context, criteria.providerId, criteria.location)

  private fun invokeBppSelect(): Either<BppError, ProtocolAckResponse> {
    return bppService.select(
      contextFactory.create(), //todo: a lot of places where the context factory is used but the action is wrong
      bppUri,
      "venugopala stores",
      ProtocolLocationFactory.idLocation(1),
      IdFactory.forItems(IdFactory.forProvider(1), 1).map { ProtocolSelectedItemFactory.create(it) }
    )
  }

  private fun invokeBppInit(): Either<BppError, ProtocolAckResponse> {
    return bppService.init(
      context = contextFactory.create(),
      bppUri = bppUri,
      providerId = "padma coffee works",
      billingInfo  = ProtocolBillingFactory.create(),
      providerLocation = ProtocolSelectMessageSelectedProviderLocations("A-11 Vedanta, High Street, 435667"),
      deliveryInfo = DeliveryDtoFactory.create(),
      items = listOf(OrderItemDtoFactory.create(bppUri, "padma coffee works","123"))
    )
  }

  private fun invokeBppConfirm(): Either<BppError, ProtocolAckResponse> {
    return bppService.confirm(
      context = contextFactory.create(),
      bppUri = bppUri,
      providerId = "padma coffee works",
      billingInfo  = ProtocolBillingFactory.create(),
      items = listOf(OrderItemDtoFactory.create(bppUri, "padma coffee works","123")),
      providerLocation = ProtocolSelectMessageSelectedProviderLocations("A-11 Vedanta, High Street, 435667"),
      deliveryInfo = DeliveryDtoFactory.create(),
      payment = OrderPayment(23.3)
    )
  }

  private fun getSelectRequest() = ProtocolSelectRequest(
    contextFactory.create(),
    ProtocolSelectRequestMessage(
      selected = ProtocolSelectMessageSelected(
        provider = ProtocolProvider(
          id = "venugopala stores",
          locations = listOf(ProtocolLocationFactory.idLocation(1))
        ),
        items = IdFactory.forItems(IdFactory.forProvider(1), 1).map { ProtocolSelectedItemFactory.create(it) }
      )
    )
  )


  private fun getInitRequest() = ProtocolInitRequest(
    context = contextFactory.create(),
    message = ProtocolInitRequestMessage(
      order = ProtocolOrder(
        provider = ProtocolSelectMessageSelectedProvider(
          id = "padma coffee works",
          locations = listOf(ProtocolSelectMessageSelectedProviderLocations("A-11 Vedanta, High Street, 435667"))
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
          customer = HboCustomer(HboCustomer.HboPerson(name = "Test"))
        ),
        addOns = emptyList(),
        offers = emptyList()
      )
    )
  )

  private fun getConfirmRequest() = ProtocolConfirmRequest(
    context = contextFactory.create(),
    message = ProtocolConfirmRequestMessage(
      order = ProtocolOrder(
        provider = ProtocolSelectMessageSelectedProvider(
          id = "padma coffee works",
          locations = listOf(ProtocolSelectMessageSelectedProviderLocations("A-11 Vedanta, High Street, 435667"))
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
          customer = HboCustomer(HboCustomer.HboPerson(name = "Test"))
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
