package org.beckn.one.sandbox.bap.client.shared.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.beckn.one.sandbox.bap.message.entities.AddDeliveryAddressDao
import org.beckn.one.sandbox.bap.message.entities.BillingDetailsDao
import org.beckn.one.sandbox.bap.message.entities.DeliveryAddressDao
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError

data class AccountDetailsResponse(
  override val context: ProtocolContext?,
  @JsonIgnore val userId: String?,
  override val error: ProtocolError?,
  @JsonProperty("user_phone") val phone: String? = null,
  @JsonProperty("user_email") val email: String? = null,
  @JsonProperty("user_name") val name: String? = null,
  @JsonProperty("delivery_addresses") var address : List<DeliveryAddressResponse> ? = null,
  @JsonProperty("billing_info") var billing : List<BillingDetailsResponse>? =  null,
  ):ClientResponse


