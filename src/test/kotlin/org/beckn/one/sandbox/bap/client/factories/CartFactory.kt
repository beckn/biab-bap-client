package org.beckn.one.sandbox.bap.client.factories

import org.beckn.one.sandbox.bap.client.shared.dtos.CartDto
import org.beckn.one.sandbox.bap.client.shared.dtos.CartItemDto

class CartFactory {
  companion object {
    val BPP_ID = "www.local-coffee-house.in"

    fun create(
      bpp1Uri: String = BPP_ID,
      bpp2Uri: String = bpp1Uri,
      provider1Id: String = "venugopala stores",
      provider1Location: List<String> = listOf("venugopala stores location 1"),
      provider2Id: String = provider1Id,
      provider2Location: List<String> = provider1Location,
      items: List<CartItemDto> = listOf(
        CartItemFactory.cothasCoffee(
          bppId = bpp1Uri,
          providerId = provider1Id,
          providerLocation = provider1Location
        ),
        CartItemFactory.malgudiCoffee(
          bppId = bpp2Uri,
          providerId = provider2Id,
          providerLocation = provider2Location
        ),
      )
    ) = CartDto(
      items = items
    )

  }
}