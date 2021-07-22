package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.Default

data class ClientContext @Default constructor(
  val transactionId: String,
  val bppId: String? = null,
)
