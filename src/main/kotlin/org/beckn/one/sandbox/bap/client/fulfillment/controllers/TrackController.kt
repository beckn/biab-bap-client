package org.beckn.one.sandbox.bap.client.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.client.services.TrackService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackController @Autowired constructor(
  val contextFactory: ContextFactory,
  val trackService: TrackService,
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @RequestMapping("/client/v1/track")
  @ResponseBody
  fun track(@RequestBody request: TrackRequestDto): ResponseEntity<ProtocolAckResponse> {
    val context = contextFactory.create(action = ProtocolContext.Action.TRACK)
    return trackService.track(context, request)
      .fold(
        {
          log.error("Error when getting tracking information: {}", it)
          mapToErrorResponse(it, context)
        },
        {
          log.info("Successfully initiated track api. Message: {}", it)
          ResponseEntity.ok(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
        }
      )
  }

  private fun mapToErrorResponse(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(ProtocolAckResponse(context = context, message = it.message(), error = it.error()))

}