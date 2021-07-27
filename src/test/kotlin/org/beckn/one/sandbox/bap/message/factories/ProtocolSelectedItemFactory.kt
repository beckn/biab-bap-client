package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.*
import java.math.BigDecimal

object ProtocolSelectedItemFactory {
  fun create(itemId: String) = ProtocolSelectedItem(
    id = "Item_$itemId",
    quantity = ProtocolItemQuantityAllocated(count = 1, measure = ProtocolScalar(BigDecimal.valueOf(100), "INR")
    )
  )
}


object ProtocolOnSelectedItemFactory {
  fun create(itemId: String) = ProtocolOnSelectedItem(
    id = "Item_$itemId",
    quantity = ProtocolItemQuantity(
      selected = ProtocolItemQuantityAllocated(
        count = 1, measure = ProtocolScalar(BigDecimal.valueOf(100), "INR")
      )
    )
  )
}