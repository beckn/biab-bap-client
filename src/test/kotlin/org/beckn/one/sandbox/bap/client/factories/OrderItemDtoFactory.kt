package org.beckn.one.sandbox.bap.client.factories

import org.beckn.one.sandbox.bap.client.dtos.CartItemProviderDto
import org.beckn.one.sandbox.bap.client.dtos.OrderItemDto
import org.beckn.protocol.schemas.ProtocolScalar
import org.beckn.protocol.schemas.ProtocolItemQuantityAllocated
import java.math.BigDecimal
import java.util.*

class OrderItemDtoFactory {
  companion object {
    fun create(bppId: String, providerId: String, uuid: String = getUuid()) =
      OrderItemDto(
        id = uuid,
        bppId = bppId,
        quantity = ProtocolItemQuantityAllocated(
          count = 1,
          measure = ProtocolScalar(
            value = BigDecimal.valueOf(1),
            unit = "kg"
          )
        ),
        provider = CartItemProviderDto(
          id = providerId,
          locations = listOf("13.001581,77.5703686")
        )
      )

    private fun getUuid() = UUID.randomUUID().toString()
  }
}