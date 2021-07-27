package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.*

object ProtocolOnInitMessageInitializedFactory {

  fun create(id: Int, numberOfItems: Int): ProtocolOnInitMessageInitialized {
    val providerId = IdFactory.forProvider(id)
    val itemIds = IdFactory.forItems(providerId, numberOfItems)
    return ProtocolOnInitMessageInitialized(
      provider = ProtocolOnInitMessageInitializedProvider(id = providerId),
      providerLocation = ProtocolOnInitMessageInitializedProviderLocation(id = "location-$id"),
      items = itemIds.map {
        ProtocolOnInitMessageInitializedItems(
          id = it,
          quantity = ProtocolItemQuantityAllocated(count = 2)
        )
      },
      addOns = null,
      offers = null,
      billing = ProtocolBillingFactory.create(),
      fulfillment = ProtocolFulfillmentFactory.create(id),
      quote = ProtocolQuotationFactory.quoteForItems(itemIds)
    )
  }
}