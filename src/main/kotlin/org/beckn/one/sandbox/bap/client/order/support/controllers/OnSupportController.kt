package org.beckn.one.sandbox.bap.client.order.support.controllers

import org.beckn.one.sandbox.bap.client.shared.controllers.AbstractOnPollController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSupportResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnSupportController @Autowired constructor(
  onPollService: GenericOnPollService<ProtocolOnSupport, ClientSupportResponse>,
  contextFactory: ContextFactory
) : AbstractOnPollController<ProtocolOnSupport, ClientSupportResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_support")
  @ResponseBody
  fun onSupportOrderV1(
    @RequestParam messageId: String
  ): ResponseEntity<out ClientResponse> = onPoll(messageId)
}