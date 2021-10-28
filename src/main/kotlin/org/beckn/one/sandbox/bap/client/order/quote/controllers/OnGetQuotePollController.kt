package org.beckn.one.sandbox.bap.client.order.quote.controllers

import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.controllers.AbstractOnPollController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientQuoteResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ProtocolOnSelect
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnGetQuotePollController @Autowired constructor(
  val onPollService: GenericOnPollService<ProtocolOnSelect, ClientQuoteResponse>,
  val contextFactory: ContextFactory,
  private val protocolClient: ProtocolClient
) : AbstractOnPollController<ProtocolOnSelect, ClientQuoteResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_get_quote")
  @ResponseBody
  fun onGetQuoteV1(@RequestParam messageId: String): ResponseEntity<out ClientResponse> =
    onPoll(messageId, protocolClient.getSelectResponsesCall(messageId))

  @RequestMapping("/client/v2/on_get_quote")
  @ResponseBody
  fun onGetQuoteV2(@RequestParam messageIds: String): ResponseEntity<out List<ClientResponse>> {

    if (messageIds.isNotEmpty() && messageIds.trim().isNotEmpty()) {
      val messageIdArray = messageIds.split(",")
      var okResponseQuotes: MutableList<ClientQuoteResponse> = ArrayList()

      if (messageIdArray.isNotEmpty()) {
        for (msgId in messageIdArray) {

          onPollService.onPoll(
            contextFactory.create(messageId = msgId),
            protocolClient.getSelectResponsesCall(msgId)
          ).fold(
            {
              okResponseQuotes.add(
                ClientQuoteResponse(
                  context = contextFactory.create(messageId = msgId),
                  error = it.error(), message = null
                )
              )
            }, {
              okResponseQuotes.add(it)
            }
          )
        }
        return ResponseEntity.ok(okResponseQuotes)
      } else {
        return mapToErrorResponse(BppError.BadRequestError)
      }
    } else {
      return mapToErrorResponse(BppError.BadRequestError)
    }
  }

  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
      listOf(
        ClientQuoteResponse(
          error = it.error(),
          context = null
        )
      )
    )
}