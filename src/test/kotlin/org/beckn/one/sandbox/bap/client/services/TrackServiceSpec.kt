package org.beckn.one.sandbox.bap.client.services

import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.dtos.ClientContext
import org.beckn.one.sandbox.bap.client.dtos.TrackRequestDto
import org.beckn.one.sandbox.bap.client.errors.TrackError
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.message.services.MessageService
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.ProtocolTrackRequestMessage
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoMoreInteractions

class TrackServiceSpec : DescribeSpec() {
  private val context = ContextFactoryInstance.create().create()
  private val registryService = mock(RegistryService::class.java)
  private val messageService = mock(MessageService::class.java)
  private val bppService = mock(BppService::class.java)
  private val trackService = TrackService(
    registryService = registryService,
    messageService = messageService,
    bppService = bppService,
  )

  init {
    describe("Track") {
      it("should validate that bpp id is not null") {
        val trackResponse = trackService.track(
          context = context,
          request = TrackRequestDto(
            context = ClientContext(transactionId = UuidFactory().create(), bppId = null),
            message = ProtocolTrackRequestMessage(orderId = "Order Id 1")
          )
        )

        trackResponse shouldBeLeft TrackError.BppIdNotPresent
        verifyNoMoreInteractions(registryService)
        verifyNoMoreInteractions(bppService)
        verifyNoMoreInteractions(messageService)
      }
    }
  }
}