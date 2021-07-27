package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.*
import java.time.OffsetDateTime

object ProtocolOrderFactory {
  fun create(id: Int, numberOfItems: Int): ProtocolOrder {
    val itemIds = IdFactory.forItems(IdFactory.forProvider(id), 3)
    return ProtocolOrder(
      provider = ProtocolSelectMessageSelectedProvider(
        id = IdFactory.forProvider(id),
        locations = listOf(ProtocolSelectMessageSelectedProviderLocations(IdFactory.forLocation(id)))
      ),
      items = itemIds.mapNotNull {
        ProtocolSelectMessageSelectedItems(
          id = it,
          quantity = ProtocolItemQuantityAllocated(count = 2)
        )
      },
      addOns = listOf(),
      offers = listOf(),
      billing = ProtocolBillingFactory.create(),
      fulfillment = ProtocolFulfillmentFactory.create(id),
      quote = ProtocolQuotationFactory.quoteForItems(itemIds),
      payment = ProtocolPaymentFactory.create(),
      id = IdFactory.forProvider(id),
      state = "IN_PROGRESS",
      createdAt = OffsetDateTime.now(fixedClock),
      updatedAt = OffsetDateTime.now(fixedClock)
    )
  }
}