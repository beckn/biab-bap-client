package org.beckn.one.sandbox.bap.client.order.suport.services

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceSupportSpec : DescribeSpec() {
  private val bppServiceClientFactory = Mockito.mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = Mockito.mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory, Mockito.mock(ObjectMapper::class.java))
  private val bppServiceClient: BppClient = Mockito.mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Support") {
      Mockito.`when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      Mockito.`when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val supportRequest = getSupportRequest()

      beforeEach {
        Mockito.reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp support call fails with an exception") {
        Mockito.`when`(bppServiceClient.support(getSupportRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppSupport()

        response shouldBeLeft BppError.Internal
        Mockito.verify(bppServiceClient).support(getSupportRequest())
      }

      it("should return bpp internal server error when bpp support call returns null body") {
        Mockito.`when`(bppServiceClient.support(supportRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppSupport()

        response shouldBeLeft BppError.NullResponse
        Mockito.verify(bppServiceClient).support(getSupportRequest())
      }

      it("should return bpp internal server error when bpp support call returns nack response body") {
        val context = contextFactory.create()
        Mockito.`when`(bppServiceClient.support(supportRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppSupport()

        response shouldBeLeft BppError.Nack
        Mockito.verify(bppServiceClient).support(getSupportRequest())
      }
    }
  }

  private fun invokeBppSupport(): Either<BppError, ProtocolAckResponse> {
    return bppService.support(
      context = contextFactory.create(),
      bppUri = bppUri,
      refId = "abc123"
    )
  }

  private fun getSupportRequest() = ProtocolSupportRequest(
    context = contextFactory.create(),
    message = ProtocolSupportRequestMessage(
      refId = "abc123"
    )
  )

}