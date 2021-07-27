package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.Default
import org.beckn.protocol.schemas.ProtocolQuotation
import org.beckn.protocol.schemas.ProtocolScalar

data class GetQuoteRequestDto @Default constructor(
  val context: ClientContext,
  val message: GetQuoteRequestMessageDto,
)

data class GetQuoteRequestMessageDto @Default constructor(
  val cart: CartDto
)

data class CartDto @Default constructor(
  val items: List<CartItemDto>? = null
)

data class CartItemDto @Default constructor(
  val id: String,
  val quantity: CartSelectedItemQuantity,
  val bppId: String,
  val provider: CartItemProviderDto,
  val quote: ProtocolQuotation? = null
)

data class CartSelectedItemQuantity @Default constructor(
  val count: Int,
  val measure: ProtocolScalar? = null
)

data class CartItemProviderDto @Default constructor(
  val id: String,
  val locations: List<String>? = null
)
