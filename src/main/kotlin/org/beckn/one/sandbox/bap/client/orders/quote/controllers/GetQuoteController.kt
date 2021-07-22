package org.beckn.one.sandbox.bap.client.orders.quote.controllers

import org.beckn.one.sandbox.bap.client.orders.quote.services.QuoteService
import org.beckn.one.sandbox.bap.client.shared.dtos.GetQuoteRequestDto
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolContext.Action.SELECT
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GetQuoteController @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val quoteService: QuoteService
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PutMapping("/client/v1/get_quote")
  @ResponseBody
  fun getQuote(@RequestBody request: GetQuoteRequestDto): ResponseEntity<ProtocolAckResponse> {
    val context = getContext(request.context.transactionId)
    return quoteService.getQuote(context, request.message.cart)
      .fold(
        {
          log.error("Error when getting quote: {}", it)
          mapToErrorResponse(it, context)
        },
        {
          log.info("Successfully initiated get quote. Message: {}", it)
          ResponseEntity.ok(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
        }
      )
  }

  private fun mapToErrorResponse(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(ProtocolAckResponse(context = context, message = it.message(), error = it.error()))

  private fun getContext(transactionId: String) = contextFactory.create(action = SELECT, transactionId = transactionId)
}