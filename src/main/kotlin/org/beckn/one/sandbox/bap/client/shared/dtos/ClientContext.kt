package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.Default
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory

data class ClientContext @Default constructor(
  val transactionId: String = UuidFactory().create(),
  val bppId: String? = null,
)
