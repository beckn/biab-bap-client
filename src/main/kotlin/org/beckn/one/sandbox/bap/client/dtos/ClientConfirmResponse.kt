package org.beckn.one.sandbox.bap.client.dtos

import org.beckn.one.sandbox.bap.schemas.ProtocolContext
import org.beckn.one.sandbox.bap.schemas.ProtocolError
import org.beckn.one.sandbox.bap.schemas.ProtocolOnConfirmMessage

data class ClientConfirmResponse(
  override val context: ProtocolContext,
  val message: ProtocolOnConfirmMessage? = null,
  override val error: ProtocolError? = null
): ClientResponse