package org.beckn.one.sandbox.bap.client.fulfillment.track.controllers

import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.controllers.AbstractOnPollController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientTrackResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnTrack
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnTrackPollController(
  onPollService: GenericOnPollService<ProtocolOnTrack, ClientTrackResponse>,
  contextFactory: ContextFactory,
  val protocolClient: ProtocolClient
) : AbstractOnPollController<ProtocolOnTrack, ClientTrackResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_track")
  @ResponseBody
  fun onTrack(@RequestParam messageId: String): ResponseEntity<out ClientResponse> = onPoll(messageId, protocolClient.getTrackResponsesCall(messageId))
}
