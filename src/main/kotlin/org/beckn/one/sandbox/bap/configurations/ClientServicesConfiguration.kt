package org.beckn.one.sandbox.bap.configurations

import org.beckn.one.sandbox.bap.client.discovery.mappers.ClientCatalogMapper
import org.beckn.one.sandbox.bap.client.discovery.mappers.SearchClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.confirm.mappers.ConfirmClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.init.mapper.InitClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.quote.mapper.QuoteClientResponseMapper
import org.beckn.one.sandbox.bap.client.fulfillment.track.mappers.TrackClientResponseMapper
import org.beckn.one.sandbox.bap.client.order.support.mappers.SupportClientResponseMapper
import org.beckn.one.sandbox.bap.client.shared.dtos.*
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.client.shared.services.GenericProtocolClientService
import org.beckn.protocol.schemas.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientServicesConfiguration @Autowired constructor(
  private val clientCatalogMapper: ClientCatalogMapper
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

  fun searchProtocolClientService() = GenericProtocolClientService<ProtocolOnSearch>()

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

}