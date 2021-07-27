package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolPayment
import java.net.URI

object ProtocolPaymentFactory {

  fun create() = ProtocolPayment(
    uri = URI("http://host.pay.co.in"),
    tlMethod = ProtocolPayment.TlMethod.POST,
    params = mapOf("nonce" to "JXNASN"),
    type = ProtocolPayment.Type.ONORDER,
    status = ProtocolPayment.Status.PAID,
    time = ProtocolTimeFactory.fixedTimestamp("Paid on")
  )

}