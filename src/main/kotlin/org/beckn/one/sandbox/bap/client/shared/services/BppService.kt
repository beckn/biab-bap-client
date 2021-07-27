package org.beckn.one.sandbox.bap.client.shared.services

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.fasterxml.jackson.databind.ObjectMapper
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.shared.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
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
  private val bppServiceClientFactory: BppClientFactory,
  private val objectMapper: ObjectMapper,
) {
  private val log: Logger = LoggerFactory.getLogger(BppService::class.java)

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
              item = ProtocolIntentItem(descriptor = ProtocolIntentItemDescriptor(name = criteria.searchString)),
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
    bppServiceClient: BppClient,
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
    return bppServiceClient.select(selectRequest).execute()
  }

  private fun isInternalServerError(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.code() == HttpStatus.INTERNAL_SERVER_ERROR.value()

  private fun isBodyNull(httpResponse: Response<ProtocolAckResponse>) = httpResponse.body() == null

  private fun isAckNegative(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.body()!!.message.ack.status == ResponseStatus.NACK

  fun init(
    context: ProtocolContext,
    bppUri: String,
    order: OrderDto
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Init API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse =
        invokeBppInitApi(
          bppServiceClient = bppServiceClient,
          context = context,
          order = order
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
    bppServiceClient: BppClient,
    context: ProtocolContext,
    order: OrderDto
  ): Response<ProtocolAckResponse> {
    val initRequest = ProtocolInitRequest(
      context = context,
      ProtocolInitRequestMessage(
        order = ProtocolOrder(
          provider = ProtocolSelectMessageSelectedProvider(
            id = order.items!!.first().provider.id,
            locations = listOf(ProtocolSelectMessageSelectedProviderLocations(id = order.items.first().provider.locations!!.first()))
          ),
          items = order.items.map { ProtocolSelectMessageSelectedItems(id = it.id, quantity = it.quantity) },
          billing = order.billingInfo,
          fulfillment = ProtocolFulfillment(
            end = ProtocolFulfillmentEnd(
              contact = ProtocolContact(
                phone = order.deliveryInfo.phone,
                email = order.deliveryInfo.email
              ), location = order.deliveryInfo.location
            ),
            type = order.deliveryInfo.type,
            customer = ProtocolCustomer(ProtocolPerson(name = order.deliveryInfo.name))
          ),
          addOns = emptyList(),
          offers = emptyList(),
        )
      )
    )
    log.info("Init API request body: {}", objectMapper.writeValueAsString(initRequest))
    return bppServiceClient.init(initRequest).execute()
  }

  private fun getFulfillmentFilter(criteria: SearchCriteria) =
    when {
      hasText(criteria.deliveryLocation) ->
        ProtocolFulfillment(end = ProtocolFulfillmentEnd(location = ProtocolLocation(gps = criteria.deliveryLocation)))
      else -> null
    }

  fun confirm(
    context: ProtocolContext,
    bppUri: String,
    order: OrderDto
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Confirm API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse =
        invokeBppConfirmApi(
          bppServiceClient = bppServiceClient,
          context = context,
          order = order
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
    bppServiceClient: BppClient,
    context: ProtocolContext,
    order: OrderDto
  ): Response<ProtocolAckResponse> {
    val confirmRequest = ProtocolConfirmRequest(
      context = context,
      ProtocolConfirmRequestMessage(
        order = ProtocolOrder(
          provider = ProtocolSelectMessageSelectedProvider(
            id = order.items!!.first().provider.id,
            locations = listOf(ProtocolSelectMessageSelectedProviderLocations(id = order.items.first().provider.locations!!.first()))
          ),
          items = order.items.map { ProtocolSelectMessageSelectedItems(id = it.id, quantity = it.quantity) },
          billing = order.billingInfo,
          fulfillment = ProtocolFulfillment(
            end = ProtocolFulfillmentEnd(
              contact = ProtocolContact(
                phone = order.deliveryInfo.phone,
                email = order.deliveryInfo.email
              ), location = order.deliveryInfo.location
            ),
            type = "home_delivery",
            customer = ProtocolCustomer(person = ProtocolPerson(name = order.deliveryInfo.name))
          ),
          addOns = emptyList(),
          offers = emptyList(),
          payment = ProtocolPayment(
            params = mapOf("amount" to order.payment!!.paidAmount.toString()),
            status = ProtocolPayment.Status.PAID
          )
        )
      )
    )
    log.info("Confirm API request body: {}", confirmRequest)
    return bppServiceClient.confirm(confirmRequest).execute()
  }

  fun track(bppUri: String, context: ProtocolContext, request: TrackRequestDto): Either<BppError, ProtocolAckResponse> =
    Either.catch {
      log.info("Invoking Track API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse = bppServiceClient.track(ProtocolTrackRequest(context = context, message = request.message))
        .execute()
      log.info("BPP Track API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        isInternalServerError(httpResponse) -> Left(BppError.Internal)
        isBodyNull(httpResponse) -> Left(BppError.NullResponse)
        isAckNegative(httpResponse) -> Left(BppError.Nack)
        else -> Right(httpResponse.body()!!)
      }
    }.mapLeft {
      log.error("Error when invoking BPP Track API", it)
      BppError.Internal
    }

  fun support(
    bppUri: String,
    context: ProtocolContext,
    refId: String
  ): Either<BppError, ProtocolAckResponse> = Either.catch {
    log.info("Invoking support API on BPP: {}", bppUri)
    val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
    val httpResponse =
      bppServiceClient.support(
        ProtocolSupportRequest(
          context = context,
          message = ProtocolSupportRequestMessage(refId = refId)
        )
      ).execute()
    log.info("BPP support API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
    return when {
      isInternalServerError(httpResponse) -> Left(BppError.Internal)
      isBodyNull(httpResponse) -> Left(BppError.NullResponse)
      isAckNegative(httpResponse) -> Left(BppError.Nack)
      else -> Right(httpResponse.body()!!)
    }
  }.mapLeft {
    log.error("Error when invoking BPP Track API", it)
    BppError.Internal
  }

}