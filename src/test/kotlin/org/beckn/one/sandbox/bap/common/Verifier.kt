package org.beckn.one.sandbox.bap.client.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.external.domains.Subscriber
import org.beckn.one.sandbox.bap.client.external.registry.SubscriberLookupRequest
import org.beckn.one.sandbox.bap.common.City
import org.beckn.one.sandbox.bap.common.Country
import org.beckn.one.sandbox.bap.common.Domain
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.litote.kmongo.eq

class Verifier(
  val objectMapper: ObjectMapper,
  val messageRepository: GenericRepository<MessageDao>,
) {
  fun verifyThatMessageForRequestIsPersisted(response: ProtocolAckResponse, type: MessageDao.Type) {
    val savedMessage = messageRepository.findOne(MessageDao::id eq response.context?.messageId)
    savedMessage shouldNotBe null
    savedMessage?.id shouldBe response.context?.messageId
    savedMessage?.type shouldBe type
  }


  fun verifyThatMessageWasNotPersisted(response: ProtocolAckResponse) {
    val savedMessage = messageRepository.findOne(MessageDao::id eq response.context?.messageId)
    savedMessage shouldBe null
  }

  fun verifyThatSubscriberLookupApiWasInvoked(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi.verify(
      postRequestedFor(urlEqualTo("/lookup"))
        .withRequestBody(
          equalToJson(
            objectMapper.writeValueAsString(
              SubscriberLookupRequest(
                subscriber_id = bppApi.baseUrl(),
                type = Subscriber.Type.BPP,
                domain = Domain.LocalRetail.value,
                country = Country.India.value,
                city = City.Bengaluru.value
              )
            )
          )
        )
    )
  }

}