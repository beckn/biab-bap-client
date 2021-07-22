package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.shared.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceTrackSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory)
  private val bppServiceClient: BppClient = mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Track") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val trackRequest = getProtocolTrackRequest()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp track call fails with an exception") {
        `when`(bppServiceClient.track(getProtocolTrackRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppTrack()

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).track(getProtocolTrackRequest())
      }

      it("should return bpp internal server error when bpp track call returns null body") {
        `when`(bppServiceClient.track(trackRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppTrack()

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).track(getProtocolTrackRequest())
      }

      it("should return bpp internal server error when bpp track call returns nack response body") {
        val context = contextFactory.create(action = ProtocolContext.Action.TRACK)
        `when`(bppServiceClient.track(trackRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppTrack()

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).track(getProtocolTrackRequest())
      }
    }
  }

  private fun invokeBppTrack(): Either<BppError, ProtocolAckResponse> {
    return bppService.track(
      bppUri,
      contextFactory.create(action = ProtocolContext.Action.TRACK),
      TrackRequestDto(
        context = ClientContext(bppId = bppUri, transactionId = ""),
        message = getProtocolTrackRequest().message
      )
    )
  }

  private fun getProtocolTrackRequest() = ProtocolTrackRequest(
    context = contextFactory.create(action = ProtocolContext.Action.TRACK),
    message = ProtocolTrackRequestMessage(
      orderId = "order id 1"
    )
  )
}
