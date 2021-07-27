package org.beckn.one.sandbox.bap.client.order.init.controllers

import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.controllers.AbstractOnPollController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientInitResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnInit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnInitOrderController @Autowired constructor(
  onPollService: GenericOnPollService<ProtocolOnInit, ClientInitResponse>,
  contextFactory: ContextFactory,
  val protocolClient: ProtocolClient
) : AbstractOnPollController<ProtocolOnInit, ClientInitResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_initialize_order")
  @ResponseBody
  fun onInitOrderV1(
    @RequestParam messageId: String
  ): ResponseEntity<out ClientResponse> = onPoll(messageId, protocolClient.getInitResponsesCall(messageId))
}