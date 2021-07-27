package org.beckn.one.sandbox.bap.client.discovery.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.external.provider.BppClient
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.factories.SearchRequestFactory
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.BppService
import org.beckn.one.sandbox.bap.common.factories.ContextFactoryInstance
import org.beckn.one.sandbox.bap.schemas.factories.UuidFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ResponseMessage
import org.mockito.Mockito.*
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class BppServiceSearchSpec : DescribeSpec() {
  private val bppServiceClientFactory = mock(BppClientFactory::class.java)
  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  private val uuidFactory = mock(UuidFactory::class.java)
  private val contextFactory = ContextFactoryInstance.create(uuidFactory, clock)
  private val bppService = BppService(bppServiceClientFactory, mock(ObjectMapper::class.java))
  private val bppServiceClient: BppClient = mock(BppClient::class.java)
  private val bppUri = "https://bpp1.com"

  init {
    describe("Search") {
      `when`(uuidFactory.create()).thenReturn("9056ea1b-275d-4799-b0c8-25ae74b6bf51")
      `when`(bppServiceClientFactory.getClient(bppUri)).thenReturn(bppServiceClient)
      val context = contextFactory.create()

      beforeEach {
        reset(bppServiceClient)
      }

      it("should return bpp internal server error when bpp search call fails with an exception") {
        val criteria = SearchCriteria(providerId = "venugopala stores", deliveryLocation = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.Internal
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }

      it("should return bpp internal server error when bpp search call returns null body") {
        val criteria = SearchCriteria(providerId = "venugopala stores", deliveryLocation = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.response(null)
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.NullResponse
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }

      it("should return bpp internal server error when bpp search call returns nack response body") {
        val criteria = SearchCriteria(providerId = "venugopala stores", deliveryLocation = "venugopala stores location 1")
        `when`(bppServiceClient.search(getSearchRequest(context, criteria))).thenReturn(
          Calls.response(ProtocolAckResponse(context, ResponseMessage.nack()))
        )

        val response = bppService.search(bppUri, context, criteria)

        response shouldBeLeft BppError.Nack
        verify(bppServiceClient).search(getSearchRequest(context, criteria))
      }
    }
  }

  private fun getSearchRequest(
    context: ProtocolContext,
    criteria: SearchCriteria
  ) = SearchRequestFactory.create(context = context, providerId = criteria.providerId, location = criteria.deliveryLocation)
}
