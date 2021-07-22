package org.beckn.one.sandbox.bap.client.discovery.mappers

import org.beckn.one.sandbox.bap.client.shared.dtos.ClientCatalog
import org.beckn.protocol.schemas.ProtocolCatalog
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
interface ClientCatalogMapper {
  fun protocolToClientDto(source: ProtocolCatalog): ClientCatalog
}