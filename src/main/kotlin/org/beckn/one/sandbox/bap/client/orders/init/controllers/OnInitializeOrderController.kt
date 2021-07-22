package org.beckn.one.sandbox.bap.client.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.ClientInitializeResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.services.GenericOnPollService
import org.beckn.protocol.schemas.ProtocolOnInit
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnInitializeOrderController @Autowired constructor(
  onPollService: GenericOnPollService<ProtocolOnInit, ClientInitializeResponse>,
  contextFactory: ContextFactory
) : BaseOnPollController<ProtocolOnInit, ClientInitializeResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_initialize_order")
  @ResponseBody
  fun onInitializeOrderV1(
    @RequestParam messageId: String
  ): ResponseEntity<out ClientResponse> = onPoll(messageId)
}