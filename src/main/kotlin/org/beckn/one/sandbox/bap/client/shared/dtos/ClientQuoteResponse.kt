package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ProtocolOnSelectMessageSelected

data class ClientQuoteResponse(
  override val context: ProtocolContext?,
  val message: ClientQuoteResponseMessage? = null,
  override val error: ProtocolError? = null,
) : ClientResponse

data class ClientQuoteResponseMessage(
  val quote: ProtocolOnSelectMessageSelected? = null
)
