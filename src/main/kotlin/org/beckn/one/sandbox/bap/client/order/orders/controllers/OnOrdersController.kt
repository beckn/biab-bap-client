package org.beckn.one.sandbox.bap.client.order.orders.controllers

import org.beckn.one.sandbox.bap.client.order.orders.services.OrderServices
import org.beckn.one.sandbox.bap.client.shared.dtos.BillingDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderResponse
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
class OnOrdersController @Autowired constructor(
  contextFactory: ContextFactory,
  val orderServices: OrderServices
){

  @RequestMapping("/client/v1/orders")
  @ResponseBody
  fun onOrdersList (
    @RequestParam orderId: String?,
    @RequestParam skip: Int?,
    @RequestParam limit: Int?
  ) :ResponseEntity<List<OrderResponse>>{


       return  orderServices.findAllOrders(orderId?:"", skip?:0,limit?:10).fold(
          {
  //        log.error("Error when cancelling order with BPP: {}", it)
            mapToErrorResponse(it)
          },
          {
  //        log.info("Successfully cancelled order with BPP. Message: {}", it)
            ResponseEntity.ok(it)
          }
        )
  }
  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
     listOf(
       OrderResponse(
         context = null,
         error = it.error(),
         userId = null
       )
     )
    )
}