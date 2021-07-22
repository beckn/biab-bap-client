package org.beckn.one.sandbox.bap.client.discovery.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.client.shared.services.RegistryService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.protocol.schemas.ProtocolContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText

@Service
class SearchService(
  @Autowired val registryService: RegistryService,
  @Autowired val gatewayService: GatewayService,
  @Autowired val bppService: BppService,
  @Autowired val messageService: MessageService
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun search(context: ProtocolContext, criteria: SearchCriteria): Either<HttpError, MessageDao> {
    log.info("Got search request with criteria: {} ", criteria)
    if (isBppFilterSpecified(criteria)) {
      val criteriaForExploringProviderCatalog = criteria.copy(searchString = null)
      return registryService
        .lookupBppById(criteriaForExploringProviderCatalog.bppId!!)
        .flatMap { bppService.search(it.first().subscriber_url, context, criteriaForExploringProviderCatalog) }
        .flatMap { messageService.save(MessageDao(id = context.messageId, type = MessageDao.Type.Search)) }
    }
    return registryService
      .lookupGateways()
      .flatMap { gatewayService.search(it.first(), context, criteria) }
      .flatMap { messageService.save(MessageDao(id = context.messageId, type = MessageDao.Type.Search)) }
  }

  private fun isBppFilterSpecified(criteria: SearchCriteria) =
    hasText(criteria.bppId)

}
