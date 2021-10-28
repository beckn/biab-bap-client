package org.beckn.one.sandbox.bap.client.shared.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import org.beckn.one.sandbox.bap.message.entities.AddressDao
import org.beckn.one.sandbox.bap.message.entities.OrganizationDao
import org.beckn.one.sandbox.bap.message.entities.TimeDao
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError

data class BillingDetailsResponse(
  override val context: ProtocolContext?,
  val id: String?,
  val name: String?,
  val phone: String?,
  val organization: OrganizationDao? = null,
  val address: AddressDao? = null,
  val email: String? = null,
  val time: TimeDao? = null,
  val taxNumber: String? = null,
  val createdAt: java.time.OffsetDateTime? = null,
  val updatedAt: java.time.OffsetDateTime? = null,
  val locationId: String? = null,
  @JsonIgnore val userId: String?,
  override val error: ProtocolError?,
  ):ClientResponse


