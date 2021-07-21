package org.beckn.one.sandbox.bap.client.shared.errors.validation

import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.http.HttpStatus

sealed class MultipleProviderError : HttpError {
  val moreThanOneProviderValidationError =
    ProtocolError("BAP_010", "More than one Provider's item(s) selected/initialized")
  val moreThanOneBppValidationError =
    ProtocolError("BAP_014", "More than one BPP's item(s) selected/initialized")

  object MultipleBpps : MultipleProviderError() {
    override fun status(): HttpStatus = HttpStatus.BAD_REQUEST

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = moreThanOneBppValidationError
  }

  object MultipleProviders : MultipleProviderError() {
    override fun status(): HttpStatus = HttpStatus.BAD_REQUEST

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = moreThanOneProviderValidationError
  }
}