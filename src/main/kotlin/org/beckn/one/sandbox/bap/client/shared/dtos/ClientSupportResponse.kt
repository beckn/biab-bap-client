package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ProtocolOnSupportMessage

data class ClientSupportResponse(
  override val context: ProtocolContext,
  val message: ProtocolOnSupportMessage,
  override val error: ProtocolError? = null,
) : ClientResponse
