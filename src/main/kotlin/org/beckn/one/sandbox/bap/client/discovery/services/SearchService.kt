package org.beckn.one.sandbox.bap.client.discovery.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.client.shared.services.RegistryService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolAckResponse
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
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun search(context: ProtocolContext, criteria: SearchCriteria): Either<HttpError, ProtocolAckResponse> {
    log.info("Got search request with criteria: {} ", criteria)
    if (isBppFilterSpecified(context)) {
      return registryService
        .lookupBppById(context.bppId!!)
        .flatMap { bppService.search(it.first().subscriber_url, context, criteria) }
    }
    return registryService
      .lookupGateways()
      .flatMap { gatewayService.search(it.first(), context, criteria) }
  }

  private fun isBppFilterSpecified(context: ProtocolContext) =
    hasText(context.bppId)

}
