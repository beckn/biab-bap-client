package org.beckn.one.sandbox.bap.client.factories

import org.beckn.one.sandbox.bap.client.shared.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderItemDto
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderPayment
import org.beckn.one.sandbox.bap.message.factories.ProtocolBillingFactory
import java.util.*

class OrderDtoFactory {
  companion object {
    fun create(
      id: String? = getUuid(),
      bpp1_id: String,
      bpp2_id: String = bpp1_id,
      provider1_id: String,
      provider2_id: String = provider1_id,
      payment: OrderPayment? = null,
      items: List<OrderItemDto> = listOf(
        OrderItemDtoFactory.create(bpp1_id, provider1_id),
        OrderItemDtoFactory.create(bpp2_id, provider2_id),
      )
    ) =
      OrderDto(
        id = id,
        items = items,
        billingInfo = ProtocolBillingFactory.create(),
        deliveryInfo = DeliveryDtoFactory.create(),
        payment = payment
      )

    private fun getUuid() = UUID.randomUUID().toString()
  }
}