package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolContext
import java.time.OffsetDateTime

object ProtocolContextFactory {

  val fixed = ProtocolContext(
    domain = "LocalRetail",
    country = "IN",
    action = ProtocolContext.Action.SEARCH,
    city = "Pune",
    coreVersion = "0.9.1-draft03",
    bapId = "http://host.bap.com",
    bapUri = "http://host.bap.com",
    transactionId = "222",
    messageId = "222",
    timestamp =  OffsetDateTime.now(fixedClock)
  )

}