package org.beckn.one.sandbox.bap.client.shared.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class GenericOnPollService<Protocol: ProtocolResponse, Output: ClientResponse> constructor(
  private val messageService: MessageService,
  private val responseStorageService: ResponseStorageService<Protocol>,
  private val transformer: GenericOnPollMapper<Protocol, Output>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  open fun onPoll(context: ProtocolContext): Either<HttpError, Output> {
    log.info("Got fetch request for message id: {}", context.messageId)
    return messageService
      .findById(context.messageId)
      .flatMap { responseStorageService.findByMessageId(context.messageId) }
      .flatMap { transformer.transform(it, context) }
  }

}

interface GenericOnPollMapper<in Protocol : ProtocolResponse, out Output : ClientResponse> {
  fun transform(input: List<Protocol>, context: ProtocolContext): Either<HttpError, Output>
}