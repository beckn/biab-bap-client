package org.beckn.one.sandbox.bap.client.orders.confirm.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientConfirmResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.entities.OnConfirmDao
import org.beckn.one.sandbox.bap.message.factories.ProtocolOrderFactory
import org.beckn.one.sandbox.bap.message.mappers.ContextMapper
import org.beckn.one.sandbox.bap.message.mappers.OnConfirmResponseMapper
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.beckn.protocol.schemas.ProtocolOnConfirmMessage
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
internal class OnConfirmOrderControllerSpec @Autowired constructor(
  private val confirmResponseRepo: BecknResponseRepository<OnConfirmDao>,
  private val messageRepository: GenericRepository<MessageDao>,
  private val onConfirmResponseMapper: OnConfirmResponseMapper,
  private val contextMapper: ContextMapper,
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val mockMvc: MockMvc
) : DescribeSpec() {
  val context = contextFactory.create()
  private val contextDao = contextMapper.fromSchema(context)
  private val anotherMessageId = "d20f481f-38c6-4a29-9acd-cbd1adab9ca0"
  private val protocolOnConfirm = ProtocolOnConfirm(
    context,
    message = ProtocolOnConfirmMessage(
      order = ProtocolOrderFactory.create(1, 2)
    )
  )

  init {
    describe("OnConfirm callback") {
      confirmResponseRepo.clear()
      messageRepository.insertOne(MessageDao(id = contextDao.messageId, type = MessageDao.Type.Init))
      confirmResponseRepo.insertMany(entityOnConfirmResults())

      context("when called for given message id") {
        val onInitCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_confirm_order")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", contextDao.messageId)
          )

        it("should respond with status ok") {
          onInitCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on confirm responses in body") {
          val results = onInitCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientConfirmResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnConfirm, ClientConfirmResponse>> {
          onGeneric { onPoll(any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onConfirmPollController = OnConfirmOrderController(mockOnPollService, contextFactory)
        it("should respond with failure") {
          val response = onConfirmPollController.onConfirmOrderV1(contextDao.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnConfirmResults(): List<OnConfirmDao> {
    val onInitDao = onConfirmResponseMapper.protocolToEntity(protocolOnConfirm)
    return listOf(
      onInitDao,
      onInitDao,
      onInitDao.copy(context = contextDao.copy(messageId = anotherMessageId))
    )
  }
}