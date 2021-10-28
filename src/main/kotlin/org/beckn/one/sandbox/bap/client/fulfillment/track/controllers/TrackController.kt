package org.beckn.one.sandbox.bap.client.fulfillment.track.controllers

import org.beckn.one.sandbox.bap.client.fulfillment.track.services.TrackService
import org.beckn.one.sandbox.bap.client.shared.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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

  @RequestMapping("/client/v2/track")
  @ResponseBody
  fun trackV2(@RequestBody trackRequestList: List<TrackRequestDto>): ResponseEntity<List<ProtocolAckResponse>> {

    if(!trackRequestList.isNullOrEmpty()){
      var okResponseTrack : MutableList<ProtocolAckResponse> = ArrayList()
      for (trackRequest in trackRequestList) {
        val context = contextFactory.create(action = ProtocolContext.Action.TRACK)
        trackService.track(context, trackRequest)
          .fold(
            {
              log.error("Error when getting tracking information: {}", it)
              okResponseTrack.add( ProtocolAckResponse(
                context = context,
                message = it.message(),
                error = it.error()
              ))
            },
            {
              log.info("Successfully initiated track api. Message: {}", it)
              okResponseTrack.add(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
            }
          )
      }
      return ResponseEntity.ok(okResponseTrack)
    }else{
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
          listOf(
            ProtocolAckResponse(
              context = null, message = ResponseMessage.nack(),
              error = ProtocolError(code = "400", message = HttpStatus.BAD_REQUEST.reasonPhrase)
            )
          )
        )
    }
  }

  private fun mapToErrorResponse(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(ProtocolAckResponse(context = context, message = it.message(), error = it.error()))

}