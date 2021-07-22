package org.beckn.one.sandbox.bap.client.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.message.factories.IdFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolLocationFactory
import org.beckn.one.sandbox.bap.message.factories.ProtocolSelectedItemFactory
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.*
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceSelectSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory)
  private val bppServiceClient: BppClient = mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Select") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val selectRequest = getSelectRequest()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp select call fails with an exception") {
        `when`(bppServiceClient.select(getSelectRequest())).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).select(getSelectRequest())
      }

      it("should return bpp internal server error when bpp select call returns null body") {
        `when`(bppServiceClient.select(selectRequest)).thenReturn(
          Calls.response(null)
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).select(getSelectRequest())
      }

      it("should return bpp internal server error when bpp select call returns nack response body") {
        val context = contextFactory.create()
        `when`(bppServiceClient.select(selectRequest)).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = invokeBppSelect()

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).select(getSelectRequest())
      }
    }
  }

  private fun invokeBppSelect(): Either<BppError, ProtocolAckResponse> {
    return bppService.select(
      contextFactory.create(), //todo: a lot of places where the context factory is used but the action is wrong
      bppUri,
      "venugopala stores",
      ProtocolLocationFactory.idLocation(1),
      IdFactory.forItems(IdFactory.forProvider(1), 1).map { ProtocolSelectedItemFactory.create(it) }
    )
  }

  private fun getSelectRequest() = ProtocolSelectRequest(
    contextFactory.create(),
    ProtocolSelectRequestMessage(
      selected = ProtocolSelectMessageSelected(
        provider = ProtocolProvider(
          id = "venugopala stores",
          locations = listOf(ProtocolLocationFactory.idLocation(1))
        ),
        items = IdFactory.forItems(IdFactory.forProvider(1), 1).map { ProtocolSelectedItemFactory.create(it) }
      )
    )
  )
}
