package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderItemDto
import org.beckn.one.sandbox.bap.client.errors.validation.MultipleProviderError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.protocol.schemas.ProtocolContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InitializeOrderService @Autowired constructor(
  private val messageService: MessageService,
  private val bppService: BppService,//todo: might require a mapper
  private val registryService: RegistryService,
  private val log: Logger = LoggerFactory.getLogger(InitializeOrderService::class.java)
) {
  fun initializeOrder(
      context: ProtocolContext,
      order: OrderDto
  ): Either<HttpError, MessageDao?> {
    log.info("Got initialize order request. Context: {}, Order: {}", context, order)
    if (order.items.isNullOrEmpty()) {
      log.info("Empty order received, no op. Order: {}", order)
      return Either.Right(null)
    }

    if (areMultipleBppItemsSelected(order.items)) {
      log.info("Order contains items from more than one BPP, returning error. Order: {}", order)
      return Either.Left(MultipleProviderError.MultipleBpps)
    }

    if (areMultipleProviderItemsSelected(order.items)) {
      log.info("Order contains items from more than one provider, returning error. Cart: {}", order)
      return Either.Left(MultipleProviderError.MultipleProviders)
    }

    return registryService.lookupBppById(order.items.first().bppId)
      .flatMap {
        bppService.initialize(
          context,
          bppUri = it.first().subscriber_url,
          order = order
        )
      }
      .flatMap {
        messageService.save(MessageDao(id = context.messageId, type = MessageDao.Type.Init))
      }

  }

  private fun areMultipleProviderItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.provider.id }.size > 1

  private fun areMultipleBppItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.bppId }.size > 1

}
