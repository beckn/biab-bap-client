package org.beckn.one.sandbox.bap.client.dtos

import org.beckn.one.sandbox.bap.Default
import org.beckn.protocol.schemas.ProtocolBilling
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolItemQuantityAllocated

data class OrderRequestDto @Default constructor(
    val context: ClientContext,
    val message: OrderDto
)

data class OrderDto @Default constructor(
  val id: String? = null,
  val items: List<OrderItemDto>? = null,
  val deliveryInfo: DeliveryDto,
  val billingInfo: ProtocolBilling,
  val payment: OrderPayment? = null
)

data class OrderItemDto @Default constructor(
    val id: String,
    val bppId: String,
    val quantity: ProtocolItemQuantityAllocated,
    val provider: CartItemProviderDto
) {
}