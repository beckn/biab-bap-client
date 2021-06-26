package org.beckn.one.sandbox.bap.client.services

import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.dtos.ClientSearchResponse
import org.beckn.one.sandbox.bap.client.dtos.ClientSearchResponseMessage
import org.beckn.one.sandbox.bap.message.entities.*
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.schemas.ProtocolCatalog
import org.beckn.one.sandbox.bap.schemas.ProtocolContext
import org.beckn.one.sandbox.bap.schemas.ProtocolSearchResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class GenericOnReplyServiceSpec @Autowired constructor(
  private val onSearchPollService: GenericOnPollService<ProtocolSearchResponse, ClientSearchResponse>,
  private val searchResultRepo: BecknResponseRepository<SearchResponse>,
  private val messageRepository: GenericRepository<Message>
) : DescribeSpec() {
  private val fixedClock = Clock.fixed(
    Instant.parse("2018-11-30T18:35:24.00Z"),
    ZoneId.of("Asia/Calcutta")
  )
  private val entityContext = Context(
    domain = "LocalRetail",
    country = "IN",
    action = Context.Action.SEARCH,
    city = "Pune",
    coreVersion = "0.9.1-draft03",
    bapId = "http://host.bap.com",
    bapUri = "http://host.bap.com",
    transactionId = "222",
    messageId = "222",
    timestamp = LocalDateTime.now(fixedClock)
  )

  private val context = ProtocolContext(
    domain = "LocalRetail",
    country = "IN",
    action = ProtocolContext.Action.SEARCH,
    city = "Pune",
    coreVersion = "0.9.1-draft03",
    bapId = "http://host.bap.com",
    bapUri = "http://host.bap.com",
    transactionId = "222",
    messageId = "222",
    timestamp = LocalDateTime.now(fixedClock)
  )

  init {
    describe("GenericOnReplyService") {
      searchResultRepo.clear()
      messageRepository.insertOne(Message(id = context.messageId, type = Message.Type.Search))
      searchResultRepo.insertMany(entitySearchResults())

      it("should return search results for given message id in context") {
        val response = onSearchPollService.onReply(context)
        response.shouldBeRight(ClientSearchResponse(
          context = context,
          message = ClientSearchResponseMessage(
            catalogs = listOf(ProtocolCatalog(), ProtocolCatalog())
          )
        ))
      }
    }
  }

  fun entitySearchResults(): List<SearchResponse> {
    val entitySearchResponse = SearchResponse(
      context = entityContext,
      message = SearchResponseMessage(Catalog())
    )
    return listOf(entitySearchResponse, entitySearchResponse, entitySearchResponse.copy(context = entityContext.copy(messageId = "123")))
  }

}