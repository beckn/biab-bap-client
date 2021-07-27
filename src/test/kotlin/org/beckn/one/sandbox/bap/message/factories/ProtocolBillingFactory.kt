package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolAddress
import org.beckn.protocol.schemas.ProtocolBilling
import org.beckn.protocol.schemas.ProtocolOrganization

object ProtocolBillingFactory {

  fun create() = ProtocolBilling(
    name = "ICIC",
    phone = "9890098900",
    organization = ProtocolOrganization(name = "IC", cred = "ajsgdysdxasg!!"),
    address = ProtocolAddress(
      door = "A-11",
      building = "Vedanta",
      street = "High Street",
      areaCode = "435667"
    ),
    email = "abc@icic",
    time = ProtocolTimeFactory.fixedDays("Working Days")
  )
}