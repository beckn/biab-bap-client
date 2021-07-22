package org.beckn.one.sandbox.bap.client.dtos

import org.beckn.protocol.schemas.Default
import org.beckn.protocol.schemas.ProtocolTrackRequestMessage

data class TrackRequestDto @Default constructor(
  val context: ClientContext,
  val message: ProtocolTrackRequestMessage,
)
