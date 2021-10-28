package org.beckn.one.sandbox.bap.client.support.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.OrderStatusDto
import org.beckn.one.sandbox.bap.client.support.services.SupportService
import org.beckn.one.sandbox.bap.client.shared.dtos.SupportRequestDto
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class SupportController @Autowired constructor(
    private val contextFactory: ContextFactory,
    private val supportService: SupportService
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/get_support")
  @ResponseBody
  fun getSupportV1(
    @RequestBody supportRequest: SupportRequestDto
  ): ResponseEntity<ProtocolAckResponse> {
    val context =
      getContext(supportRequest.context.transactionId) // might not matter as the context might have different transaction id
    return supportService.getSupport(
      context = context,
      supportRequestMessage = supportRequest.message,
      bppId = supportRequest.context.bppId
    ).fold(
      {
        log.error("Error when getting support for refId: {}", it)
        mapToErrorResponse(it, context)
      },
      {
        log.info("Successfully retrieved support for refId. Message: {}", it)
        ResponseEntity.ok(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
      }
    )
  }

  @PostMapping("/client/v2/get_support")
  @ResponseBody
  fun getSupportV2(
    @RequestBody supportRequestList: List<SupportRequestDto>
  ): ResponseEntity<List<ProtocolAckResponse>> {

    if(!supportRequestList.isNullOrEmpty()) {
      var okResponseSupport : MutableList<ProtocolAckResponse> = ArrayList()
      for (supportRequest in supportRequestList) {
        val context =
          getContext(supportRequest.context.transactionId) // might not matter as the context might have different transaction id

        supportService.getSupport(
          context = context,
          supportRequestMessage = supportRequest.message,
          bppId = supportRequest.context.bppId
        ).fold(
          {
            log.error("Error when getting support for refId: {}", it)
            okResponseSupport.add( ProtocolAckResponse(
              context = context,
              message = it.message(),
              error = it.error()
            ))
          },
          {
            log.info("Successfully retrieved support for refId. Message: {}", it)
            okResponseSupport.add(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
          }
        )
      }
      return ResponseEntity.ok(okResponseSupport)
    }else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
          listOf(
            ProtocolAckResponse(
              context = null, message = ResponseMessage.nack(),
              error = ProtocolError(code = "400", message = HttpStatus.BAD_REQUEST.reasonPhrase)
            )
          )
        )
    }
  }

  private fun mapToErrorResponse(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(
      ProtocolAckResponse(
        context = context,
        message = it.message(),
        error = it.error()
      )
    )

  private fun getContext(transactionId: String) =
    contextFactory.create(action = ProtocolContext.Action.SUPPORT, transactionId = transactionId)
}