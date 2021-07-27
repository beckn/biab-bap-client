package org.beckn.one.sandbox.bap.client.order.confirm.mappers

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientConfirmResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnConfirm

class ConfirmClientResponseMapper : GenericOnPollMapper<ProtocolOnConfirm, ClientConfirmResponse> {
  override fun transform(
    input: List<ProtocolOnConfirm>,
    context: ProtocolContext
  ): Either<HttpError, ClientConfirmResponse> =
    Either.Right(
      ClientConfirmResponse(
        context = context,
        message = input.first().message
      )
    )
}