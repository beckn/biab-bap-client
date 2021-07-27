package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolProvider

object ProtocolProviderFactory {

  fun create(id: Int): ProtocolProvider {
    val providerId = IdFactory.forProvider(id)
    return ProtocolProvider(
      id = providerId,
      descriptor = ProtocolDescriptorFactory.create("Retail-provider", providerId),
      time = ProtocolTimeFactory.fixedTimestamp("fixed-time"),
      locations = listOf(
        ProtocolLocationFactory.cityLocation(1),
        ProtocolLocationFactory.cityLocation(2).copy(city = ProtocolCityFactory.pune)
      ),
      tags = mapOf("key 1" to "value 1")
    )
  }

}