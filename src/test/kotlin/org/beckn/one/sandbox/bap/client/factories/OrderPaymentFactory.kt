package org.beckn.one.sandbox.bap.client.factories

import org.beckn.one.sandbox.bap.client.shared.dtos.OrderPayment

class OrderPaymentFactory {
  companion object {
    fun create(
      status: OrderPayment.Status = OrderPayment.Status.PAID,
    ) =
      OrderPayment(
        paidAmount = 23.3,
        status = status,
        transactionId = "abc"
      )
  }
}