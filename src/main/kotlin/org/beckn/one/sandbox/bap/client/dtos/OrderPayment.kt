package org.beckn.one.sandbox.bap.client.dtos

import org.beckn.one.sandbox.bap.Default

data class OrderPayment @Default constructor(
  val paidAmount: Double
)