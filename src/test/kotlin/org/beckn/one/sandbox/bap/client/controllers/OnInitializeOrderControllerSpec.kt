package org.beckn.one.sandbox.bap.client.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.dtos.ClientInitResponse
import org.beckn.one.sandbox.bap.client.services.GenericOnPollService
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.entities.OnInitDao
import org.beckn.one.sandbox.bap.message.factories.ProtocolOnInitMessageInitializedFactory
import org.beckn.one.sandbox.bap.message.mappers.ContextMapper
import org.beckn.one.sandbox.bap.message.mappers.OnInitResponseMapper
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.protocol.schemas.*
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
internal class OnInitializeOrderControllerSpec @Autowired constructor(
  private val initializeResponseRepo: BecknResponseRepository<OnInitDao>,
  private val messageRepository: GenericRepository<MessageDao>,
  private val onInitResponseMapper: OnInitResponseMapper,
  private val contextMapper: ContextMapper,
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val mockMvc: MockMvc
) : DescribeSpec() {
  val context = contextFactory.create()
  private val contextDao = contextMapper.fromSchema(context)
  private val anotherMessageId = "d20f481f-38c6-4a29-9acd-cbd1adab9ca0"
  private val protocolOnInit = ProtocolOnInit(
    context,
    message = ProtocolOnInitMessage(ProtocolOnInitMessageInitializedFactory.create(id = 1, numberOfItems = 1))
  )

  init {
    describe("OnInitialize callback") {
      initializeResponseRepo.clear()
      messageRepository.insertOne(MessageDao(id = contextDao.messageId, type = MessageDao.Type.Init))
      initializeResponseRepo.insertMany(entityOnInitResults())

      context("when called for given message id") {
        val onInitCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_initialize_order")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", contextDao.messageId)
          )

        it("should respond with status ok") {
          onInitCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on init responses in body") {
          val results = onInitCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientInitResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnInit, ClientInitResponse>> {
          onGeneric { onPoll(any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onInitPollController = OnInitializeOrderController(mockOnPollService, contextFactory)
        it("should respond with failure") {
          val response = onInitPollController.onInitializeOrderV1(contextDao.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnInitResults(): List<OnInitDao> {
    val onInitDao = onInitResponseMapper.protocolToEntity(protocolOnInit)
    return listOf(
      onInitDao,
      onInitDao,
      onInitDao.copy(context = contextDao.copy(messageId = anotherMessageId))
    )
  }
}