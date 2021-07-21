package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.Default
import org.beckn.protocol.schemas.ProtocolLocation

data class DeliveryDto @Default constructor(
  val name: String,
  val phone: String,
  val email: String,
  val location: ProtocolLocation
)