package org.beckn.one.sandbox.bap.configurations

import org.beckn.one.sandbox.bap.client.discovery.mappers.ClientCatalogMapper
import org.beckn.one.sandbox.bap.client.discovery.mappers.SearchClientResponseMapper
import org.beckn.one.sandbox.bap.client.orders.confirm.mappers.ConfirmClientResponseMapper
import org.beckn.one.sandbox.bap.client.orders.init.mappers.InitClientResponseMapper
import org.beckn.one.sandbox.bap.client.orders.quote.mappers.QuoteClientResponseMapper
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientConfirmResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientInitializeResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientQuoteResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSearchResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.beckn.protocol.schemas.ProtocolOnInit
import org.beckn.protocol.schemas.ProtocolOnSearch
import org.beckn.protocol.schemas.ProtocolOnSelect
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
  fun forInitResults(): GenericOnPollMapper<ProtocolOnInit, ClientInitializeResponse> =
    InitClientResponseMapper()

  @Bean
  fun forConfirmResults(): GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse> =
    ConfirmClientResponseMapper()

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
    @Autowired transformer: GenericOnPollMapper<ProtocolOnInit, ClientInitializeResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)

  @Bean
  fun confirmResultReplyService(
    @Autowired messageService: MessageService,
    @Autowired responseStorageService: ResponseStorageService<ProtocolOnConfirm>,
    @Autowired transformer: GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse>
  ) = GenericOnPollService(messageService, responseStorageService, transformer)
}