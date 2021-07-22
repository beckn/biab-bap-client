package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ProtocolOnInitMessage

data class ClientInitializeResponse(
    override val context: ProtocolContext,
    val message: ProtocolOnInitMessage? = null,
    override val error: ProtocolError? = null
): ClientResponse
