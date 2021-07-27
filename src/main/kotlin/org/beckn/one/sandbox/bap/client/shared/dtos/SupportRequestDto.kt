package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.Default

data class SupportRequestDto @Default constructor(
  val context: ClientContext,
  val message: SupportRequestMessage
)

data class SupportRequestMessage @Default constructor(
  val refId: String,
  val bppId: String
)