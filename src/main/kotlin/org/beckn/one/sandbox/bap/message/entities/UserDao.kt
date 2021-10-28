package org.beckn.one.sandbox.bap.message.entities

import org.beckn.protocol.schemas.Default

data class UserDao @Default constructor(
  val userPhone: String? = null,
  val userEmail: String? = null,
  val userName: String? = null,
  val deliveryAddresses : List<DeliveryAddressDao> ? = null,
  val billingInfo : List<BillingDetailsDao>? = null,
)
