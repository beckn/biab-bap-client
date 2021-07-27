package org.beckn.one.sandbox.bap.client.order.support.mappers

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSupportResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnSupport
import org.beckn.protocol.schemas.ProtocolOnSupportMessage

class SupportClientResponseMapper : GenericOnPollMapper<ProtocolOnSupport, ClientSupportResponse> {
  override fun transform(
    input: List<ProtocolOnSupport>,
    context: ProtocolContext
  ): Either<HttpError, ClientSupportResponse> =
    Either.Right(
      ClientSupportResponse(
        context = context,
        message = ProtocolOnSupportMessage(phone = input.first().message?.phone, email = input.first().message?.email, uri = input.first().message?.uri)
      )
    )
}