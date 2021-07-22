package org.beckn.one.sandbox.bap.client.orders.init.mappers

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientInitializeResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnInit

class InitClientResponseMapper : GenericOnPollMapper<ProtocolOnInit, ClientInitializeResponse> {
  override fun transform(
    input: List<ProtocolOnInit>,
    context: ProtocolContext
  ): Either<HttpError, ClientInitializeResponse> =
    Either.Right(
      ClientInitializeResponse(
        context = context,
        message = input.first().message
      )
    )
}