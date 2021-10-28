package org.beckn.one.sandbox.bap.client.order.quote.controllers

import org.beckn.one.sandbox.bap.client.order.quote.services.QuoteService
import org.beckn.one.sandbox.bap.client.shared.dtos.GetQuoteRequestDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolContext.Action.SELECT
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GetQuoteController @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val quoteService: QuoteService
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/get_quote")
  @ResponseBody
  fun getQuoteV1(@RequestBody request: GetQuoteRequestDto): ResponseEntity<ProtocolAckResponse> {
    val context = getContext(request.context.transactionId)
    return quoteService.getQuote(context, request.message.cart)
      .fold(
        {
          log.error("Error when getting quote: {}", it)
          mapToErrorResponseV1(it, context)
        },
        {
          log.info("Successfully initiated get quote. Message: {}", it)
          ResponseEntity.ok(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
        }
      )
  }

  private fun mapToErrorResponseV1(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(ProtocolAckResponse(context = context, message = it.message(), error = it.error()))


  @PostMapping("/client/v2/get_quote")
  @ResponseBody
  fun getQuoteV2(@RequestBody request: List<GetQuoteRequestDto>): ResponseEntity<List<ProtocolAckResponse>> {
    var okResponseQuotes : MutableList<ProtocolAckResponse> = ArrayList()

    if(!request.isNullOrEmpty()){
      for( quoteRequest:GetQuoteRequestDto in request){
        val context = getContext(quoteRequest.context.transactionId)
        quoteService.getQuote(context, quoteRequest.message.cart)
          .fold(
            {
              log.error("Error when getting quote: {}", it)
              okResponseQuotes.add(ProtocolAckResponse(context = context, message = it.message(), error = it.error()))
            },
            {
              log.info("Successfully initiated get quote. Message: {}", it)
              okResponseQuotes.add(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
            }
          )
      }
      return ResponseEntity.ok(okResponseQuotes)
    }else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
          listOf(ProtocolAckResponse(
            context = null,message = ResponseMessage.nack() ,
            error = ProtocolError(code = "400",message = HttpStatus.BAD_REQUEST.reasonPhrase)))
        )
    }
  }
  private fun getContext(transactionId: String) = contextFactory.create(action = SELECT, transactionId = transactionId)
}