package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.*
import java.time.LocalDateTime

data class ClientSearchResponse(
  override val context: ProtocolContext,
  val message: ClientSearchResponseMessage? = null,
  override val error: ProtocolError? = null,
) : ClientResponse

data class ClientSearchResponseMessage(
  val catalogs: List<ClientCatalog>? = null,
)

data class ClientCatalog @Default constructor(
  val bppDescriptor: ProtocolDescriptor? = null,
  val bppProviders: List<ProtocolProviderCatalog>? = null,
  val bppCategories: List<ProtocolCategory>? = null,
  val exp: LocalDateTime? = null,
  val bppId: String? = null,
)
