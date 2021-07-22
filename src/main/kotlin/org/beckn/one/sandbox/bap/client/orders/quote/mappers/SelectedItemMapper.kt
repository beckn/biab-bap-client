package org.beckn.one.sandbox.bap.client.orders.quote.mappers

import org.beckn.one.sandbox.bap.client.shared.dtos.CartItemDto
import org.beckn.protocol.schemas.ProtocolSelectedItem
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
interface SelectedItemMapper {
  @Mapping(target = "quantity", source = "quantity")
  fun dtoToProtocol(dto: CartItemDto): ProtocolSelectedItem
}