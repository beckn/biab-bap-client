package org.beckn.one.sandbox.bap.client.shared.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import org.beckn.protocol.schemas.Default

data class OrderPayment @Default constructor(
  val paidAmount: Double,
  val status: Status,
  val transactionId: String
) {
  enum class Status(val value: String) {
    @JsonProperty("PAID")
    PAID("PAID"),
    @JsonProperty("NOT-PAID")
    NOTPAID("NOT-PAID");
  }
}