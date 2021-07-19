package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.dtos.*
import org.beckn.one.sandbox.bap.client.mappers.ClientCatalogMapper
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.*

interface GenericOnPollTransformer<in Protocol : ProtocolResponse, out Output : ClientResponse> {
  fun transform(input: List<Protocol>, context: ProtocolContext): Either<HttpError, Output>
}

class SearchClientSearchResponseMapper(
  private val clientCatalogMapper: ClientCatalogMapper
) : GenericOnPollTransformer<ProtocolOnSearch, ClientSearchResponse> {
  override fun transform(
    input: List<ProtocolOnSearch>,
    context: ProtocolContext
  ): Either<HttpError, ClientSearchResponse> =
    Either.Right(
      ClientSearchResponse(
        context = context,
        message = ClientSearchResponseMessage(
          catalogs = input.mapNotNull { response ->
            val catalog = response.message?.catalog?.let(clientCatalogMapper::protocolToClientDto)
            catalog?.copy(bppId = response.context.bppId)
          })
      )
    )
}

class QuoteClientQuoteResponseMapper : GenericOnPollTransformer<ProtocolOnSelect, ClientQuoteResponse> {
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

class InitClientResponseMapper : GenericOnPollTransformer<ProtocolOnInit, ClientInitResponse> {
  override fun transform(
    input: List<ProtocolOnInit>,
    context: ProtocolContext
  ): Either<HttpError, ClientInitResponse> =
    Either.Right(
      ClientInitResponse(
        context = context,
        message = input.first().message
      )
    )
}

class ConfirmClientResponseMapper : GenericOnPollTransformer<ProtocolOnConfirm, ClientConfirmResponse> {
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
