package org.beckn.one.sandbox.bap.client.shared.dtos

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonIgnore
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.message.entities.*
import org.beckn.protocol.schemas.Default
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ProtocolOrderStatusRequestMessage

data class OrderResponse @Default constructor(
  val provider: SelectMessageSelectedProviderDao? = null,
  val items: List<SelectMessageSelectedItemsDao>? = null,
  val addOns: List<SelectMessageSelectedAddOnsDao>? = null,
  val offers: List<SelectMessageSelectedOffersDao>? = null,
  val billing: BillingDao? = null,
  val fulfillment: FulfillmentDao? = null,
  val quote: QuotationDao? = null,
  val payment: PaymentDao? = null,
  val id: String? = null,
  val state: String? = null,
  val createdAt: java.time.OffsetDateTime? = null,
  val updatedAt: java.time.OffsetDateTime? = null,
  val transactionId: String? = null,
  val messageId: String? = null,
  override val context: ProtocolContext?,
  override val error: ProtocolError?,
  @JsonIgnore val userId: String?,
  ) :ClientResponse

