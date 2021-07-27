package org.beckn.one.sandbox.bap.client.order.suport.controllers

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.beckn.one.sandbox.bap.client.order.support.controllers.OnSupportController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientSupportResponse
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.entities.MessageDao
import org.beckn.one.sandbox.bap.message.entities.OnSupportDao
import org.beckn.one.sandbox.bap.message.mappers.ContextMapper
import org.beckn.one.sandbox.bap.message.mappers.OnSupportResponseMapper
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSupport
import org.beckn.protocol.schemas.ProtocolOnSupportMessage
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
class OnSupportControllerSpec @Autowired constructor(
  private val supportResponseRepo: BecknResponseRepository<OnSupportDao>,
  private val messageRepository: GenericRepository<MessageDao>,
  private val onSupportResponseMapper: OnSupportResponseMapper,
  private val contextMapper: ContextMapper,
  private val contextFactory: ContextFactory,
  private val mapper: ObjectMapper,
  private val mockMvc: MockMvc
) : DescribeSpec() {
  val context = contextFactory.create()
  private val contextDao = contextMapper.fromSchema(context)
  private val anotherMessageId = "d20f481f-38c6-4a29-9acd-cbd1adab9ca0"
  private val protocolOnSupport = ProtocolOnSupport(
    context,
    message = ProtocolOnSupportMessage(phone = "1234567890")
  )

  init {
    describe("OnInitialize callback") {
      supportResponseRepo.clear()
      messageRepository.insertOne(MessageDao(id = contextDao.messageId, type = MessageDao.Type.Support))
      supportResponseRepo.insertMany(entityOnInitResults())

      context("when called for given message id") {
        val onSupportCallBack = mockMvc
          .perform(
            MockMvcRequestBuilders.get("/client/v1/on_support")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .param("messageId", contextDao.messageId)
          )

        it("should respond with status ok") {
          onSupportCallBack.andExpect(MockMvcResultMatchers.status().isOk)
        }

        it("should respond with all on support responses in body") {
          val results = onSupportCallBack.andReturn()
          val body = results.response.contentAsString
          val clientResponse = mapper.readValue(body, ClientSupportResponse::class.java)
          clientResponse.message shouldNotBe null
        }
      }

      context("when failure occurs during request processing") {
        val mockOnPollService = mock<GenericOnPollService<ProtocolOnSupport, ClientSupportResponse>> {
          onGeneric { onPoll(any()) }.thenReturn(Either.Left(DatabaseError.OnRead))
        }
        val onSupportPollController = OnSupportController(mockOnPollService, contextFactory)
        it("should respond with failure") {
          val response = onSupportPollController.onSupportOrderV1(contextDao.messageId)
          response.statusCode shouldBe DatabaseError.OnRead.status()
        }
      }
    }
  }

  fun entityOnInitResults(): List<OnSupportDao> {
    val onSupportDao = onSupportResponseMapper.protocolToEntity(protocolOnSupport)
    return listOf(
      onSupportDao,
      onSupportDao,
      onSupportDao.copy(context = contextDao.copy(messageId = anotherMessageId))
    )
  }

}