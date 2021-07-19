package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import org.beckn.one.sandbox.bap.client.dtos.DeliveryDto
import org.beckn.one.sandbox.bap.client.dtos.OrderItemDto
import org.beckn.one.sandbox.bap.client.dtos.OrderPayment
import org.beckn.one.sandbox.bap.client.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.external.provider.BppServiceClient
import org.beckn.protocol.schemas.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import retrofit2.Response

@Service
class BppService @Autowired constructor(
  private val bppServiceClientFactory: BppServiceClientFactory,
) {
  private val log: Logger = LoggerFactory.getLogger(BppService::class.java)

  fun select(
      context: ProtocolContext,
      bppUri: String,
      providerId: String,
      providerLocation: ProtocolLocation,
      items: List<ProtocolSelectedItem>
  ): Either<BppError, ProtocolAckResponse> {
    return Either
      .catch {
        log.info("Invoking Select API on BPP: {}", bppUri)
        val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
        val httpResponse = invokeBppSelectApi(bppServiceClient, context, providerId, providerLocation, items)
        log.info("BPP Select API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
        return when {
          isInternalServerError(httpResponse) -> Left(BppError.Internal)
          isBodyNull(httpResponse) -> Left(BppError.NullResponse)
          isAckNegative(httpResponse) -> Left(BppError.Nack)
          else -> Right(httpResponse.body()!!)
        }
      }.mapLeft {
        log.error("Error when initiating select", it)
        BppError.Internal
      }
  }

  private fun invokeBppSelectApi(
      providerServiceClient: BppServiceClient,
      context: ProtocolContext,
      providerId: String,
      providerLocation: ProtocolLocation,
      items: List<ProtocolSelectedItem>
  ): Response<ProtocolAckResponse> {
    val selectRequest = ProtocolSelectRequest(
      context = context,
      ProtocolSelectRequestMessage(
        selected = ProtocolSelectMessageSelected(
          provider = ProtocolProvider(id = providerId, locations = listOf(providerLocation)),
          items = items
        )
      )
    )
    log.info("Select API request body: {}", selectRequest)
    return providerServiceClient.select(selectRequest).execute()
  }

  private fun isInternalServerError(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.code() == HttpStatus.INTERNAL_SERVER_ERROR.value()

  private fun isBodyNull(httpResponse: Response<ProtocolAckResponse>) = httpResponse.body() == null

  private fun isAckNegative(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.body()!!.message.ack.status == ResponseStatus.NACK

  fun init(
      context: ProtocolContext,
      bppUri: String,
      providerId: String,
      billingInfo: ProtocolBilling,
      providerLocation: ProtocolSelectMessageSelectedProviderLocations,
      deliveryInfo: DeliveryDto,
      items: List<OrderItemDto>
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Init API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse =
        invokeBppInitApi(
          bppServiceClient = bppServiceClient,
          context = context,
          providerId = providerId,
          billingInfo = billingInfo,
          providerLocation = providerLocation,
          deliveryInfo = deliveryInfo,
          items = items
        )
      log.info("BPP init API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        isInternalServerError(httpResponse) -> Left(BppError.Internal)
        isBodyNull(httpResponse) -> Left(BppError.NullResponse)
        isAckNegative(httpResponse) -> Left(BppError.Nack)
        else -> Right(httpResponse.body()!!)
      }
    }.mapLeft {
      log.error("Error when initiating init", it)
      BppError.Internal
    }
  }

  private fun invokeBppInitApi(
      bppServiceClient: BppServiceClient,
      context: ProtocolContext,
      providerId: String,
      billingInfo: ProtocolBilling,
      providerLocation: ProtocolSelectMessageSelectedProviderLocations,
      deliveryInfo: DeliveryDto,
      items: List<OrderItemDto>
  ): Response<ProtocolAckResponse> {
    val initRequest = ProtocolInitRequest(
      context = context,
      ProtocolInitRequestMessage(
        order = ProtocolOrder(
          provider = ProtocolSelectMessageSelectedProvider(
            id = providerId,
            locations = listOf(providerLocation)
          ),
          items = items.map { ProtocolSelectMessageSelectedItems(id = it.id, quantity = it.quantity) },
          billing = billingInfo,
          fulfillment = ProtocolFulfillment(
            end = ProtocolFulfillmentEnd(
              contact = ProtocolContact(
                phone = deliveryInfo.phone,
                email = deliveryInfo.email
              ), location = deliveryInfo.location
            ),
            type = "home_delivery",
            customer = ProtocolCustomer(ProtocolPerson(name = deliveryInfo.name))
          ),
          addOns = emptyList(),
          offers = emptyList(),
        )
      )
    )
    log.info("Init API request body: {}", initRequest)
    return bppServiceClient.init(initRequest).execute()
  }

  fun search(bppUri: String, context: ProtocolContext, criteria: SearchCriteria)
      : Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Search API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      log.info("Initiated Search for context: {}", context)
      val httpResponse = bppServiceClient.search(
        ProtocolSearchRequest(
          context,
          ProtocolSearchRequestMessage(
            ProtocolIntent(
              queryString = criteria.searchString,
              provider = ProtocolProvider(id = criteria.providerId),
              fulfillment = getFulfillmentFilter(criteria),
            )
          )
        )
      ).execute()

      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        isInternalServerError(httpResponse) -> Left(BppError.Internal)
        httpResponse.body() == null -> Left(BppError.NullResponse)
        isAckNegative(httpResponse) -> Left(BppError.Nack)
        else -> {
          log.info("Successfully invoked search on Bpp. Response: {}", httpResponse.body())
          Right(httpResponse.body()!!)
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }

  private fun getFulfillmentFilter(criteria: SearchCriteria) =
    when {
      hasText(criteria.location) ->
        ProtocolFulfillment(end = ProtocolFulfillmentEnd(location = ProtocolLocation(gps = criteria.location)))
      else -> null
    }

  fun confirm(
    context: ProtocolContext,
    bppUri: String,
    providerId: String,
    billingInfo: ProtocolBilling,
    items: List<OrderItemDto>,
    providerLocation: ProtocolSelectMessageSelectedProviderLocations,
    deliveryInfo: DeliveryDto,
    payment: OrderPayment
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Confirm API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse =
        invokeBppConfirmApi(
          bppServiceClient = bppServiceClient,
          context = context,
          providerId = providerId,
          billingInfo = billingInfo,
          providerLocation = providerLocation,
          deliveryInfo = deliveryInfo,
          items = items,
          payment = payment
        )
      log.info("BPP confirm API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        isInternalServerError(httpResponse) -> Left(BppError.Internal)
        isBodyNull(httpResponse) -> Left(BppError.NullResponse)
        isAckNegative(httpResponse) -> Left(BppError.Nack)
        else -> Right(httpResponse.body()!!)
      }
    }.mapLeft {
      log.error("Error when initiating confirm", it)
      BppError.Internal
    }
  }

  private fun invokeBppConfirmApi(
    bppServiceClient: BppServiceClient,
    context: ProtocolContext,
    providerId: String,
    billingInfo: ProtocolBilling,
    providerLocation: ProtocolSelectMessageSelectedProviderLocations,
    deliveryInfo: DeliveryDto,
    items: List<OrderItemDto>,
    payment: OrderPayment
  ): Response<ProtocolAckResponse> {
    val confirmRequest = ProtocolConfirmRequest(
      context = context,
      ProtocolConfirmRequestMessage(
        order = ProtocolOrder(
          provider = ProtocolSelectMessageSelectedProvider(
            id = providerId,
            locations = listOf(providerLocation)
          ),
          items = items.map { ProtocolSelectMessageSelectedItems(id = it.id, quantity = it.quantity) },
          billing = billingInfo,
          fulfillment = ProtocolFulfillment(
            end = ProtocolFulfillmentEnd(
              contact = ProtocolContact(
                phone = deliveryInfo.phone,
                email = deliveryInfo.email
              ), location = deliveryInfo.location
            ),
            type = "home_delivery",
            customer = HboCustomer(HboCustomer.HboPerson(name = deliveryInfo.name))
          ),
          addOns = emptyList(),
          offers = emptyList(),
          payment = ProtocolPayment(
            params = mapOf("amount" to payment.paidAmount.toString()),
            status = ProtocolPayment.Status.PAID
          )
        )
      )
    )
    log.info("Confirm API request body: {}", confirmRequest)
    return bppServiceClient.confirm(confirmRequest).execute()
  }
}