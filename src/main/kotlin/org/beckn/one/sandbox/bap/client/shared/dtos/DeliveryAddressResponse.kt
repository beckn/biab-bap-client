package org.beckn.one.sandbox.bap.client.shared.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import org.beckn.one.sandbox.bap.message.entities.AddressDao
import org.beckn.one.sandbox.bap.message.entities.DeliveryAddressDao
import org.beckn.one.sandbox.bap.message.entities.DescriptorDao
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError

data class DeliveryAddressResponse(
  override val context: ProtocolContext?,
  @JsonIgnore val userId: String?,
  override val error: ProtocolError?,
  val id: String?,
  val descriptor: DescriptorDao? = null,
  val gps: String? = null,
  val default: Boolean? = true,
  val address: AddressDao? = null
  ):ClientResponse


