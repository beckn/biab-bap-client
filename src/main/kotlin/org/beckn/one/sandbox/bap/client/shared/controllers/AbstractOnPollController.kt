package org.beckn.one.sandbox.bap.client.shared.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import retrofit2.Call

open class AbstractOnPollController<Protocol: ProtocolResponse, Output: ClientResponse>(
  private val onPollService: GenericOnPollService<Protocol, Output>,
  private val contextFactory: ContextFactory
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun onPoll(
    messageId: String,
    call: Call<List<Protocol>>
  ): ResponseEntity<out ClientResponse> = onPollService
    .onPoll(contextFactory.create(messageId = messageId), call)
    .fold(
      {
        log.error("Error when finding response by message id. Error: {}", it)
        ResponseEntity
          .status(it.status().value())
          .body(ClientErrorResponse(context = contextFactory.create(messageId = messageId), error = it.error()))
      },
      {
        log.info("Found responses for message {}", messageId)
        ResponseEntity.ok(it)
      }
    )
}