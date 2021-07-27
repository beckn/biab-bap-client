package org.beckn.one.sandbox.bap.client.order.support.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.shared.dtos.SupportRequestMessage
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.client.shared.services.RegistryService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SupportService @Autowired constructor(
  private val bppService: BppService,
  private val registryService: RegistryService,
  private val log: Logger = LoggerFactory.getLogger(SupportService::class.java)
) {
  fun getSupport(
    context: ProtocolContext,
    supportRequestRequestMessage: SupportRequestMessage
  ): Either<HttpError, ProtocolAckResponse?> {
    log.info("Got support request for reference Id: {}", supportRequestRequestMessage.refId)

    return registryService.lookupBppById(supportRequestRequestMessage.bppId)
      .flatMap {
        bppService.support(
          bppUri = it.first().subscriber_url,
          context = context,
          refId = supportRequestRequestMessage.refId
        )
      }
  }
}
