package org.beckn.one.sandbox.bap.configurations

import org.beckn.one.sandbox.bap.client.discovery.mappers.ClientCatalogMapper
import org.beckn.one.sandbox.bap.client.discovery.mappers.SearchClientResponseMapper
import org.beckn.one.sandbox.bap.client.fulfillment.track.mappers.TrackClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.cancel.mappers.CancelClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.confirm.mappers.ConfirmClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.init.mapper.InitClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.quote.mapper.QuoteClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.status.mappers.OrderStatusClientResponseMapper
import org.beckn.one.sandbox.bap.client.rating.mappers.RatingClientResponseMapper
import org.beckn.one.sandbox.bap.client.shared.dtos.*
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.client.shared.services.GenericProtocolClientService
import org.beckn.one.sandbox.bap.client.support.mappers.SupportClientResponseMapper
import org.beckn.one.sandbox.bap.message.entities.AccountDetailsDao
import org.beckn.one.sandbox.bap.message.entities.AddDeliveryAddressDao
import org.beckn.one.sandbox.bap.message.entities.BillingDetailsDao
import org.beckn.one.sandbox.bap.message.entities.OrderDao
import org.beckn.one.sandbox.bap.message.mappers.GenericResponseMapper
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.one.sandbox.bap.message.services.ResponseStorageServiceImpl
import org.beckn.protocol.schemas.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientServicesConfiguration @Autowired constructor(
  private val clientCatalogMapper: ClientCatalogMapper,
) {
  @Bean
  fun forSearchResults(): GenericOnPollMapper<ProtocolOnSearch, ClientSearchResponse> =
    SearchClientResponseMapper(clientCatalogMapper)

  @Bean
  fun forQuoteResults(): GenericOnPollMapper<ProtocolOnSelect, ClientQuoteResponse> =
    QuoteClientResponseMapper()

  @Bean
  fun forInitResults(): GenericOnPollMapper<ProtocolOnInit, ClientInitResponse> =
    InitClientResponseMapper()

  @Bean
  fun forConfirmResults(): GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse> =
    ConfirmClientResponseMapper()

  @Bean
  fun forTrackResults(): GenericOnPollMapper<ProtocolOnTrack, ClientTrackResponse> =
    TrackClientResponseMapper()

  @Bean
  fun forSupportResults(): GenericOnPollMapper<ProtocolOnSupport, ClientSupportResponse> =
    SupportClientResponseMapper()

  @Bean
  fun forRatingResults(): GenericOnPollMapper<ProtocolOnRating, ClientRatingResponse> =
    RatingClientResponseMapper()

  @Bean
  fun forOrderStatusResults(): GenericOnPollMapper<ProtocolOnOrderStatus, ClientOrderStatusResponse> =
    OrderStatusClientResponseMapper()

  @Bean
  fun forCancelResults(): GenericOnPollMapper<ProtocolOnCancel, ClientCancelResponse> =
    CancelClientResponseMapper()

  @Bean
  fun selectProtocolClientService() = GenericProtocolClientService<ProtocolOnSelect>()

  @Bean
  fun initProtocolClientService() = GenericProtocolClientService<ProtocolOnInit>()

  @Bean
  fun confirmProtocolClientService() = GenericProtocolClientService<ProtocolOnConfirm>()

  @Bean
  fun trackProtocolClientService() = GenericProtocolClientService<ProtocolOnTrack>()

  @Bean
  fun supportProtocolClientService() = GenericProtocolClientService<ProtocolOnSupport>()

  @Bean
  fun onOrderStatusProtocolClientService() = GenericProtocolClientService<ProtocolOnOrderStatus>()

  @Bean
  fun cancelProtocolClientService() = GenericProtocolClientService<ProtocolOnCancel>()

  @Bean
  fun searchResultReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnSearch>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSearch, ClientSearchResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun quoteReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnSelect>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSelect, ClientQuoteResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun initResultReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnInit>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnInit, ClientInitResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun confirmResultReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnConfirm>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun trackReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnTrack>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnTrack, ClientTrackResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun supportResultReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnSupport>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSupport, ClientSupportResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun ratingReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnRating>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnRating, ClientRatingResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun orderStatusReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnOrderStatus>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnOrderStatus, ClientOrderStatusResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun cancelReplyService(
    @Autowired protocolService: GenericProtocolClientService<ProtocolOnCancel>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnCancel, ClientCancelResponse>
  ) = GenericOnPollService(protocolService, transformer)

  @Bean
  fun addDeliveryAddressRepo(
    @Autowired responseRepository: BecknResponseRepository<AddDeliveryAddressDao>,
    @Autowired mapper: GenericResponseMapper<DeliveryAddressResponse, AddDeliveryAddressDao>,
  ): ResponseStorageService<DeliveryAddressResponse, AddDeliveryAddressDao> = ResponseStorageServiceImpl(responseRepository,mapper)

  @Bean
  fun setBillingDetailsRepo(
    @Autowired responseRepository: BecknResponseRepository<BillingDetailsDao>,
    @Autowired mapper: GenericResponseMapper<BillingDetailsResponse, BillingDetailsDao>,
  ): ResponseStorageService<BillingDetailsResponse, BillingDetailsDao> = ResponseStorageServiceImpl(responseRepository,mapper)

  @Bean
  fun setAccountDetailsRepo(
    @Autowired responseRepository: BecknResponseRepository<AccountDetailsDao>,
    @Autowired mapper: GenericResponseMapper<AccountDetailsResponse, AccountDetailsDao>,
  ): ResponseStorageService<AccountDetailsResponse, AccountDetailsDao> = ResponseStorageServiceImpl(responseRepository,mapper)

  @Bean
  fun setOrderProtocolToDao(
    @Autowired responseRepository: BecknResponseRepository<OrderDao>,
    @Autowired mapper: GenericResponseMapper<OrderResponse, OrderDao>,
  ): ResponseStorageService<OrderResponse, OrderDao> = ResponseStorageServiceImpl(responseRepository,mapper)

}