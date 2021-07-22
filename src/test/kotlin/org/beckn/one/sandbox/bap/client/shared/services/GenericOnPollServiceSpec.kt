package org.beckn.one.sandbox.bap.client.services

import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientCatalog
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSearchResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSearchResponseMessage
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.message.entities.*
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class GenericOnPollServiceSpec @Autowired constructor(
  private val onSearchPollService: GenericOnPollService<ProtocolOnSearch, ClientSearchResponse>,
  private val searchResultRepo: BecknResponseRepository<OnSearchDao>,
  private val messageRepository: GenericRepository<MessageDao>
) : DescribeSpec() {
  private val fixedClock = Clock.fixed(
    Instant.parse("2018-11-30T18:35:24.00Z"),
    ZoneId.of("UTC")
  )
  private val entityContext = ContextDao(
    domain = "LocalRetail",
    country = "IN",
    action = ContextDao.Action.SEARCH,
    city = "Pune",
    coreVersion = "0.9.1-draft03",
    bapId = "http://host.bap.com",
    bapUri = "http://host.bap.com",
    transactionId = "222",
    messageId = "222",
    timestamp = OffsetDateTime.now(fixedClock)
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
    timestamp = OffsetDateTime.now(fixedClock)
  )

  init {
    describe("GenericOnReplyService") {
      searchResultRepo.clear()
      messageRepository.insertOne(MessageDao(id = context.messageId, type = MessageDao.Type.Search))
      searchResultRepo.insertMany(entitySearchResults())

      it("should return search results for given message id in context") {
        val response = onSearchPollService.onPoll(context)
        response.shouldBeRight(
          ClientSearchResponse(
            context = context,
            message = ClientSearchResponseMessage(
              catalogs = listOf(ClientCatalog(), ClientCatalog())
            )
          )
        )
      }
    }
  }

  fun entitySearchResults(): List<OnSearchDao> {
    val entitySearchResponse = OnSearchDao(
      context = entityContext,
      message = OnSearchMessageDao(CatalogDao())
    )
    return listOf(
      entitySearchResponse,
      entitySearchResponse,
      entitySearchResponse.copy(context = entityContext.copy(messageId = "123"))
    )
  }

}