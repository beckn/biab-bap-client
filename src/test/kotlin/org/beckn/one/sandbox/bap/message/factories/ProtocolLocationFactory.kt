package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolAddress
import org.beckn.protocol.schemas.ProtocolCity
import org.beckn.protocol.schemas.ProtocolCountry
import org.beckn.protocol.schemas.ProtocolLocation

object ProtocolLocationFactory {

  fun idLocation(id: Int) = ProtocolLocation(
    id = IdFactory.forLocation(id)
  )

  fun cityLocation(id: Int) = ProtocolLocation(
    id = IdFactory.forLocation(id),
    descriptor = ProtocolDescriptorFactory.create("location", IdFactory.forLocation(id)),
    city = ProtocolCityFactory.bangalore,
    country = ProtocolCountryFactory.india
  )

  fun addressLocation(id: Int) = ProtocolLocation(
    id = IdFactory.forLocation(id),
    descriptor = ProtocolDescriptorFactory.create("location", IdFactory.forLocation(id)),
    address = ProtocolAddress(
      door = "A-11",
      building = "Vedanta",
      street = "High Street",
      areaCode = "435667"
    )
  )
}

object ProtocolCityFactory {

  val pune = ProtocolCity(
    name = "Pune",
    code = "PUN"
  )


  val bangalore = ProtocolCity(
    name = "Bangalore",
    code = "BLR"
  )
}

object ProtocolCountryFactory {
  val india = ProtocolCountry(
    name = "INDIA",
    code = "IN"
  )
}