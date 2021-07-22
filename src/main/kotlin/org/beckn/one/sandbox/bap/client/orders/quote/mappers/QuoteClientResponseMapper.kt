package org.beckn.one.sandbox.bap.client.orders.quote.mappers

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientQuoteResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientQuoteResponseMessage
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollMapper
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnSelect

class QuoteClientResponseMapper : GenericOnPollMapper<ProtocolOnSelect, ClientQuoteResponse> {
  override fun transform(
    input: List<ProtocolOnSelect>,
    context: ProtocolContext
  ): Either<HttpError, ClientQuoteResponse> =
    Either.Right(
      ClientQuoteResponse(
        context = context,
        message = ClientQuoteResponseMessage(quote = input.first().message?.selected)
      )
    )
}