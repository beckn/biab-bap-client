package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError

interface ClientResponse {
  val context: ProtocolContext?
  val error: ProtocolError?
}

data class ClientErrorResponse(
    override val context: ProtocolContext?,
    override val error: ProtocolError? = null
) : ClientResponse