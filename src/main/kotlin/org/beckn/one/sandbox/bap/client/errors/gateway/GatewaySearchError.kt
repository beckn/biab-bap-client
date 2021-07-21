package org.beckn.one.sandbox.bap.client.shared.errors.gateway

import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.http.HttpStatus

sealed class GatewaySearchError : HttpError {
  val gatewayError = ProtocolError("BAP_003", "Gateway search returned error")
  val nullError = ProtocolError("BAP_004", "Gateway search returned null")
  val nackError = ProtocolError("BAP_005", "Gateway search returned nack")

  object Internal : GatewaySearchError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = gatewayError
  }

  object Nack : GatewaySearchError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = nackError
  }

  object NullResponse : GatewaySearchError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = nullError
  }
}