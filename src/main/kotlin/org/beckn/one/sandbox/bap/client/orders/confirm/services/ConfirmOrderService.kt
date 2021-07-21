package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.dtos.OrderItemDto
import org.beckn.one.sandbox.bap.client.dtos.OrderPayment
import org.beckn.one.sandbox.bap.client.errors.bpp.BppError
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
class ConfirmOrderService @Autowired constructor(
  private val messageService: MessageService,
  private val bppService: BppService,
  private val registryService: RegistryService,
  private val log: Logger = LoggerFactory.getLogger(ConfirmOrderService::class.java)
) {
  fun confirmOrder(
    context: ProtocolContext,
    order: OrderDto
  ): Either<HttpError, MessageDao?> {
    log.info("Got confirm order request.  Context: {}, Order: {}", context, order)

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

    if (arePaymentsPending(order.payment)) {
      log.info("Payment pending for Cart: {}", order)
      return Either.Left(BppError.PendingPayment)
    }

    return registryService.lookupBppById(order.items.first().bppId)
      .flatMap {
        bppService.confirm(
          context,
          bppUri = it.first().subscriber_url,
          order = order
        ) //todo: when payment is integrated, payment object can be passed down to get specifics of amount paid, etc
      }
      .flatMap {
        messageService.save(MessageDao(id = context.messageId, type = MessageDao.Type.Confirm))
      }

  }

  private fun arePaymentsPending(payment: OrderPayment?): Boolean {
    return payment == null || payment.status != OrderPayment.Status.PAID || payment.paidAmount <= 0
  } //todo: need to make a call with external payment gateway to see if the transaction ID is valid and payment was made

  private fun areMultipleProviderItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.provider.id }.size > 1

  private fun areMultipleBppItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.bppId }.size > 1


}
