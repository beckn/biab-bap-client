package org.beckn.one.sandbox.bap.client.order.quote.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.order.quote.mapper.SelectedItemMapper
import org.beckn.one.sandbox.bap.client.shared.dtos.CartDto
import org.beckn.one.sandbox.bap.client.shared.dtos.CartItemDto
import org.beckn.one.sandbox.bap.client.shared.errors.CartError
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.client.shared.services.RegistryService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class QuoteService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppService: BppService,
  private val selectedItemMapper: SelectedItemMapper,
) {
  private val log: Logger = LoggerFactory.getLogger(QuoteService::class.java)

  fun getQuote(context: ProtocolContext, cart: CartDto): Either<HttpError, ProtocolAckResponse?> {
    log.info("Got get quote request. Context: {}, Cart: {}", context, cart)
    if (cart.items.isNullOrEmpty()) {
      log.info("Empty cart received, not doing anything. Cart: {}", cart)
      return Either.Right(null)
    }

    if (areMultipleBppItemsSelected(cart.items)) {
      log.info("Cart contains items from more than one BPP, returning error. Cart: {}", cart)
      return Either.Left(CartError.MultipleBpps)
    }

    if (areMultipleProviderItemsSelected(cart.items)) {
      log.info("Cart contains items from more than one provider, returning error. Cart: {}", cart)
      return Either.Left(CartError.MultipleProviders)
    }
    return registryService.lookupBppById(cart.items.first().bppId)
      .flatMap { Either.Right(it.first()) }
      .flatMap {
        bppService.select(
          context,
          bppUri = it.subscriber_url,
          providerId = cart.items.first().provider.id,
          providerLocation = ProtocolLocation(id = cart.items.first().provider.locations?.first()),
          items = cart.items.map { cartItem -> selectedItemMapper.dtoToProtocol(cartItem) }
        )
      }
  }

  private fun areMultipleProviderItemsSelected(items: List<CartItemDto>) =
    items.distinctBy { it.provider.id }.size > 1

  private fun areMultipleBppItemsSelected(items: List<CartItemDto>) =
    items.distinctBy { it.bppId }.size > 1
}
