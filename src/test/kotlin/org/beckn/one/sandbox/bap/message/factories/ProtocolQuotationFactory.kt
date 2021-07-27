package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolQuotation
import org.beckn.protocol.schemas.ProtocolQuotationBreakup

object ProtocolQuotationFactory {

  fun quoteForItems(itemIds: List<String>) = ProtocolQuotation(
    price = ProtocolPriceFactory.create(),
    breakup = itemIds.map { ProtocolQuotationBreakupFactory.forItem(it) },
    ttl = "30"
  )
}

object ProtocolQuotationBreakupFactory {

  private fun create(id: String, type: ProtocolQuotationBreakup.Type) = ProtocolQuotationBreakup(
    type = type,
    refId = id,
    price = ProtocolPriceFactory.create()
  )

  fun forItem(itemId: String) = create(itemId, ProtocolQuotationBreakup.Type.ITEM)
  fun forAddon(itemId: String) = create(itemId, ProtocolQuotationBreakup.Type.ADDON)
  fun forFulfilment(itemId: String) = create(itemId, ProtocolQuotationBreakup.Type.FULFILLMENT)
  fun forOffer(itemId: String) = create(itemId, ProtocolQuotationBreakup.Type.OFFER)

}