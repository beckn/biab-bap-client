package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.message.entities.AddressDao
import org.beckn.one.sandbox.bap.message.entities.DescriptorDao
import org.beckn.one.sandbox.bap.message.entities.OrganizationDao
import org.beckn.one.sandbox.bap.message.entities.TimeDao
import org.beckn.protocol.schemas.Default
import org.beckn.protocol.schemas.ProtocolLocation

data class BillingDetailRequestDto @Default constructor(
  val name: String,
  val phone: String,
  val organization: OrganizationDao? = null,
  val address: AddressDao? = null,
  val email: String? = null,
  val taxNumber: String? = null,
  val createdAt: java.time.OffsetDateTime? = null,
  val updatedAt: java.time.OffsetDateTime? = null,
  val locationId: String? = null

)