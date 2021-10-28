package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.message.entities.AddressDao
import org.beckn.one.sandbox.bap.message.entities.DescriptorDao
import org.beckn.protocol.schemas.Default
import org.beckn.protocol.schemas.ProtocolLocation

data class DeliveryAddressRequestDto @Default constructor(
  val descriptor: DescriptorDao? = null,
  val gps: String? = null,
  val default: Boolean? = true,
  val address: AddressDao? = null
  )