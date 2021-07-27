package org.beckn.one.sandbox.bap.client.shared.errors.bap

import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.http.HttpStatus

sealed class ProtocolClientError : HttpError {
  val bapError = ProtocolError("BAP_021", "BAP Protocol client returned error")
  val nullError = ProtocolError("BAP_022", "BAP Protocol client returned null")

  object Internal : ProtocolClientError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = bapError
  }


  object NullResponse : ProtocolClientError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = nullError
  }

}