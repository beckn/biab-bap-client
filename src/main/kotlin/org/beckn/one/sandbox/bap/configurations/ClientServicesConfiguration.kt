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
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
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

  @Bean
  fun searchResultReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnSearch>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSearch, ClientSearchResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun quoteReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnSelect>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSelect, ClientQuoteResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun initResultReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnInit>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnInit, ClientInitResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun confirmResultReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnConfirm>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun trackReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnTrack>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnTrack, ClientTrackResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun supportResultReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnSupport>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnSupport, ClientSupportResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

}