package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.*
import java.time.LocalDate
import java.time.OffsetDateTime

object ProtocolFulfillmentFactory {

  fun create(id: Int) = ProtocolFulfillment(
    id = IdFactory.forFulfillment(id),
    type = "Delivery",
    state = ProtocolState(
      descriptor = ProtocolDescriptorFactory.create("Fulfillment", IdFactory.forFulfillment(id)),
      updatedAt = OffsetDateTime.now(fixedClock),
      updatedBy = "Beck"
    ),
    tracking = false,
    agent = ProtocolPersonFactory.create(),
    vehicle = ProtocolVehicle(
      category = "Sedan",
      capacity = 4,
      make = "Toyota",
      model = "Etios",
      color = "White",
      registration = "MH99ZZ9876"
    ),
    start = ProtocolFulfillmentStart(
      location = ProtocolLocationFactory.addressLocation(1),
      time = ProtocolTimeFactory.fixedTimestamp("start-on"),
      contact = ProtocolContact(phone = "9890098900", email = "ab@gmail.com")
    ),
    end = ProtocolFulfillmentEnd(
      location = ProtocolLocationFactory.addressLocation(1),
      time = ProtocolTimeFactory.fixedTimestamp("start-on"),
      contact = ProtocolContact(phone = "9890098900", email = "ab@gmail.com")
    ),
    customer = ProtocolCustomer(
      person = ProtocolPersonFactory.create()
    )
  )
}


object ProtocolPersonFactory {

  fun create() = ProtocolPerson(
    name = "Ben Beckman",
    gender = "Male",
    image = "/image.jpg",
    dob = LocalDate.now(fixedClock),
    cred = "achgsg22@@"
  )
}
