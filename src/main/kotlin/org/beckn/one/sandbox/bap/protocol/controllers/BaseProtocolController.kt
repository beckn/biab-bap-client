package org.beckn.one.sandbox.bap.protocol.controllers

import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolResponse
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

open class BaseProtocolController<Protocol: ProtocolResponse> @Autowired constructor(
  private val storage: ResponseStorageService<Protocol>
) {
  val log = LoggerFactory.getLogger(BaseProtocolController::class.java)

  fun onCallback(@RequestBody callBackActionResponse: Protocol) = storage
    .save(callBackActionResponse)
    .fold(
      ifLeft = {
        log.error("Error during persisting. Error: {}", it)
        ResponseEntity
          .status(it.status().value())
          .body(ProtocolAckResponse(callBackActionResponse.context, it.message(), it.error()))
      },
      ifRight = {
        log.info("Successfully persisted response with message id: ${callBackActionResponse.context?.messageId}")
        ResponseEntity.ok(ProtocolAckResponse(callBackActionResponse.context, ResponseMessage.ack()))
      }
    )
}