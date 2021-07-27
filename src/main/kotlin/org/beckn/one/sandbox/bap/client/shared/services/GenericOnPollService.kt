package org.beckn.one.sandbox.bap.client.shared.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call

open class GenericOnPollService<Protocol: ProtocolResponse, Output: ClientResponse> constructor(
  private val protocolService: GenericProtocolClientService<Protocol>,
  private val transformer: GenericOnPollMapper<Protocol, Output>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  open fun onPoll(context: ProtocolContext, call: Call<List<Protocol>>): Either<HttpError, Output> {
    log.info("Got fetch request for message id: {}", context.messageId)
    return protocolService.getResponse(call)
      .flatMap { transformer.transform(it, context) }
  }

}

interface GenericOnPollMapper<in Protocol : ProtocolResponse, out Output : ClientResponse> {
  fun transform(input: List<Protocol>, context: ProtocolContext): Either<HttpError, Output>
}