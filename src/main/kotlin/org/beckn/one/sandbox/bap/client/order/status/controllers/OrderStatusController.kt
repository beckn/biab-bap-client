package org.beckn.one.sandbox.bap.client.order.status.controllers

import org.beckn.one.sandbox.bap.client.order.status.services.OrderStatusService
import org.beckn.one.sandbox.bap.client.shared.dtos.GetQuoteRequestDto
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderStatusDto
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderStatusController @Autowired constructor(
  private val contextFactory: ContextFactory,
  private val orderStatusService: OrderStatusService
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/order_status")
  @ResponseBody
  fun orderStatusV1(
    @RequestBody orderStatusRequest: OrderStatusDto
  ): ResponseEntity<ProtocolAckResponse> {
    val context = getContext(orderStatusRequest.context.transactionId)
    return orderStatusService.getOrderStatus(
      context = context,
      request = orderStatusRequest
    )
      .fold(
        {
          log.error("Error when getting order status: {}", it)
          mapToErrorResponseV1(it, context)
        },
        {
          log.info("Successfully triggered order status api. Message: {}", it)
          ResponseEntity.ok(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
        }
      )
  }

  private fun mapToErrorResponseV1(it: HttpError, context: ProtocolContext) = ResponseEntity
    .status(it.status())
    .body(
      ProtocolAckResponse(
        context = context,
        message = it.message(),
        error = it.error()
      )
    )

  @PostMapping("/client/v2/order_status")
  @ResponseBody
  fun orderStatusV2(
    @RequestBody orderStatusRequest: List<OrderStatusDto>
  ): ResponseEntity<List<ProtocolAckResponse>> {
    var okResponseOrderStatus : MutableList<ProtocolAckResponse> = ArrayList()

    if(!orderStatusRequest.isNullOrEmpty()){
      for( data: OrderStatusDto in orderStatusRequest) {
        val context = getContext(data.context.transactionId)
        orderStatusService.getOrderStatus(
          context = context,
          request = data
        ).fold(
            {
              log.error("Error when getting order status: {}", it)
              okResponseOrderStatus.add( ProtocolAckResponse(
                context = context,
                message = it.message(),
                error = it.error()
              ))
            },
            {
              log.info("Successfully triggered order status api. Message: {}", it)
              okResponseOrderStatus.add( ProtocolAckResponse(
                context = context, message = ResponseMessage.ack()
              ))
            }
          )
      }
      return ResponseEntity.ok(okResponseOrderStatus)
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

  private fun getContext(transactionId: String) =
    contextFactory.create(action = ProtocolContext.Action.STATUS, transactionId = transactionId)
}