package org.beckn.one.sandbox.bap.client.factories

import org.beckn.one.sandbox.bap.client.shared.dtos.CartItemDto
import org.beckn.one.sandbox.bap.client.shared.dtos.CartItemProviderDto
import org.beckn.one.sandbox.bap.client.shared.dtos.CartSelectedItemQuantity
import org.beckn.protocol.schemas.ProtocolScalar
import java.math.BigDecimal

class CartItemFactory {
  companion object {
    fun cothasCoffee(
      bppId: String = "www.local-coffee-house.in",
      providerId: String = "venugopala stores",
      providerLocation: List<String> = listOf("$providerId location 1"),
    ) = CartItemDto(
      id = "cothas-coffee-1",
      bppId = bppId,
      provider = CartItemProviderDto(
        id = providerId,
        locations = providerLocation
      ),
      quantity = CartSelectedItemQuantity(
        count = 1,
        measure = ProtocolScalar(
          value = BigDecimal.valueOf(1),
          unit = "kg"
        )
      ),
    )

    fun malgudiCoffee(
      bppId: String = "www.local-coffee-house.in",
      providerId: String = "venugopala stores",
      providerLocation: List<String> = listOf("$providerId location 1"),
    ) = CartItemDto(
      id = "malgudi-coffee-500-gm",
      bppId = bppId,
      provider = CartItemProviderDto(
        id = providerId,
        locations = providerLocation
      ),
      quantity = CartSelectedItemQuantity(
        count = 1,
        measure = ProtocolScalar(
          value = BigDecimal.valueOf(500),
          unit = "gm"
        )
      ),
    )
  }
}