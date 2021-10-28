package org.beckn.one.sandbox.bap.message.mappers

import org.beckn.one.sandbox.bap.client.shared.dtos.*
import org.beckn.one.sandbox.bap.message.entities.*
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.beckn.protocol.schemas.ProtocolOrder
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId


interface GenericResponseMapper<Protocol : ClientResponse, Entity : BecknResponseDao> {
  fun entityToProtocol(entity: Entity): Protocol
  fun protocolToEntity(schema: Protocol): Entity
}

@Component
class DateMapper {
  fun map(instant: Instant?): OffsetDateTime? {
    return instant?.let { OffsetDateTime.ofInstant(it, ZoneId.of("UTC")) }
  }

  fun map(offset: OffsetDateTime?): Instant? {
    return offset?.toInstant()
  }
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
interface DeliveryAddressResponseMapper : GenericResponseMapper<DeliveryAddressResponse, AddDeliveryAddressDao> {
  @Mapping(ignore = true, target = "userId")
  override fun entityToProtocol(entity: AddDeliveryAddressDao): DeliveryAddressResponse

  override fun protocolToEntity(schema: DeliveryAddressResponse): AddDeliveryAddressDao
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface BillingDetailsResponseMapper : GenericResponseMapper<BillingDetailsResponse, BillingDetailsDao> {
  @Mapping(ignore = true, target = "userId")
  override fun entityToProtocol(entity: BillingDetailsDao): BillingDetailsResponse

  override fun protocolToEntity(schema: BillingDetailsResponse): BillingDetailsDao
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface AccountDetailsResponseMapper : GenericResponseMapper<AccountDetailsResponse, AccountDetailsDao> {
  @Mapping(ignore = true, target = "userId")
  override fun entityToProtocol(entity: AccountDetailsDao): AccountDetailsResponse

  override fun protocolToEntity(schema: AccountDetailsResponse): AccountDetailsDao
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface OrderResponseMapper : GenericResponseMapper<OrderResponse, OrderDao> {
  @Mapping(ignore = true, target = "userId")
  override fun entityToProtocol(entity: OrderDao): OrderResponse

  override fun protocolToEntity(schema: OrderResponse): OrderDao
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface OnConfirmResponseMapper : GenericResponseMapper<ClientConfirmResponse, OnConfirmDao> {
  override fun entityToProtocol(entity: OnConfirmDao): ClientConfirmResponse

  override fun protocolToEntity(schema: ClientConfirmResponse): OnConfirmDao
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface OnOrderProtocolToEntityOrder {
   fun protocolToEntity(entity: ProtocolOrder): OrderDao
}