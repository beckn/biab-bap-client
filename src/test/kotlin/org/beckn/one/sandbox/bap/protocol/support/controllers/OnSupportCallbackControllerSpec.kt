//package org.beckn.one.sandbox.bap.protocol.support.controllers
//
//import arrow.core.Either
//import io.kotest.core.spec.style.DescribeSpec
//import io.kotest.matchers.ints.shouldBeExactly
//import io.kotest.matchers.shouldBe
//import org.beckn.one.sandbox.bap.errors.database.DatabaseError
//import org.beckn.one.sandbox.bap.message.entities.OnSupportDao
//import org.beckn.one.sandbox.bap.message.factories.ProtocolContextFactory
//import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
//import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
//import org.beckn.protocol.schemas.ProtocolOnSupport
//import org.beckn.protocol.schemas.ProtocolOnSupportMessage
//import org.mockito.kotlin.mock
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.HttpHeaders
//import org.springframework.http.MediaType
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.context.TestPropertySource
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles(value = ["test"])
//@TestPropertySource(locations = ["/application-test.yml"])
//internal class OnSupportCallbackControllerSpec @Autowired constructor(
//  @Autowired private val mockMvc: MockMvc,
//  @Autowired private val mapper: ObjectMapper,
//  @Autowired private val onSupportResponseRepo: BecknResponseRepository<OnSupportDao>
//) : DescribeSpec() {
//  private val postOnSupportUrl = "/v1/on_support"
//  val onSupportResponse = ProtocolOnSupport(
//    context = ProtocolContextFactory.fixed,
//    message = ProtocolOnSupportMessage(
//      phone = "1234567890"
//    )
//  )
//
//  init {
//    describe("Protocol OnSupport API") {
//
//      context("when posted to with a valid response") {
//        onSupportResponseRepo.clear()
//        val postOnSupportResponse = mockMvc
//          .perform(
//            MockMvcRequestBuilders.post(postOnSupportUrl)
//              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//              .content(mapper.writeValueAsBytes(onSupportResponse))
//          )
//
//        it("should respond with status as 200") {
//          postOnSupportResponse.andExpect(MockMvcResultMatchers.status().isOk)
//        }
//
//        it("should save on support response in db") {
//          onSupportResponseRepo.findByMessageId(onSupportResponse.context.messageId).size shouldBeExactly 1
//        }
//      }
//
//      context("when error occurs when processing request") {
//        val mockService = mock<ResponseStorageService<ProtocolOnSupport>> {
//          onGeneric { save(onSupportResponse) }.thenReturn(Either.Left(DatabaseError.OnWrite))
//        }
//        val controller = OnSupportCallbackController(mockService)
//
//        it("should respond with internal server error") {
//          val response = controller.onSupport(onSupportResponse)
//          response.statusCode shouldBe DatabaseError.OnWrite.status()
//        }
//      }
//    }
//  }
//
//}