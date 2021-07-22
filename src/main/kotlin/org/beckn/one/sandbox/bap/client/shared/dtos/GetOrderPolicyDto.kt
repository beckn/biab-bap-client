package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.Default
import org.beckn.protocol.schemas.ProtocolContext

data class GetOrderPolicyDto @Default constructor(
  val context: ProtocolContext
)

