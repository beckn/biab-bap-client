package org.beckn.one.sandbox.bap.message.entities

import org.beckn.protocol.schemas.Default


data class DeliveryAddressDao @Default constructor(
  val id: String,
  val descriptor: DescriptorDao? = null,
  val gps: String? = null,
  val default: Boolean? = true,
  val address: AddressDao? = null
  )
