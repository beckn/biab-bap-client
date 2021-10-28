package org.beckn.one.sandbox.bap.client.order.status.controllers

import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.order.status.services.OnOrderStatusService
import org.beckn.one.sandbox.bap.client.shared.controllers.AbstractOnPollController
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientOrderStatusResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.client.shared.services.GenericOnPollService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.one.sandbox.bap.message.entities.OrderDao
import org.beckn.one.sandbox.bap.message.mappers.OnOrderProtocolToEntityOrder
import org.beckn.protocol.schemas.ProtocolContext
import org.beckn.protocol.schemas.ProtocolOnOrderStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
class OnOrderStatusPollController(
  onPollService: GenericOnPollService<ProtocolOnOrderStatus, ClientOrderStatusResponse>,
  val contextFactory: ContextFactory,
  val mapping: OnOrderProtocolToEntityOrder,
  val protocolClient: ProtocolClient,
  val onOrderStatusService: OnOrderStatusService
) : AbstractOnPollController<ProtocolOnOrderStatus, ClientOrderStatusResponse>(onPollService, contextFactory) {

  @RequestMapping("/client/v1/on_order_status")
  @ResponseBody
  fun onOrderStatusV1(@RequestParam messageId: String): ResponseEntity<out ClientResponse> =
    onPoll(messageId, protocolClient.getOrderStatusResponsesCall(messageId))

  @RequestMapping("/client/v2/on_order_status")
  @ResponseBody
  fun onOrderStatusV2(@RequestParam messageIds: String): ResponseEntity<out List<ClientResponse>> {

    if (messageIds.isNotEmpty() && messageIds.trim().isNotEmpty()) {
      val messageIdArray = messageIds.split(",")
      var okResponseOnOrderStatus: MutableList<ClientResponse> = ArrayList()
      if (messageIdArray.isNotEmpty()) {
        if (SecurityUtil.getSecuredUserDetail() != null) {
          val user = SecurityUtil.getSecuredUserDetail()
          for (messageId in messageIdArray) {
            val bapResult = onPoll(messageId, protocolClient.getOrderStatusResponsesCall(messageId))
            when (bapResult.statusCode.value()) {
              200 -> {
                  val resultResponse = bapResult.body as ClientOrderStatusResponse

                  val orderDao: OrderDao = mapping.protocolToEntity(resultResponse.message?.order!!)
                  orderDao.transactionId = resultResponse.context.transactionId
                  orderDao.userId = user?.uid
                  orderDao.messageId = messageId
                  onOrderStatusService.updateOrder(orderDao).fold(
                    {
                      okResponseOnOrderStatus.add(
                        ClientErrorResponse(
                          context = contextFactory.create(messageId = messageId),
                          error = it.error()
                        )
                      )
                    }, {
                      okResponseOnOrderStatus.add(resultResponse)
                    }
                  )
              }
              else -> {
                okResponseOnOrderStatus.add(
                  ClientErrorResponse(
                    context = contextFactory.create(messageId = messageId),
                    error = bapResult.body?.error
                  )
                )
              }
            }
          }
        }else{
          return mapToErrorResponseV2(BppError.AuthenticationError)
        }

        return ResponseEntity.ok(okResponseOnOrderStatus)
      } else {
        return mapToErrorResponseV2(BppError.BadRequestError)
      }
    } else {
      return mapToErrorResponseV2(BppError.BadRequestError)
    }
  }

  private fun mapToErrorResponseV2(it: HttpError, context: ProtocolContext? = null) = ResponseEntity
    .status(it.status())
    .body(
      listOf(
        ClientErrorResponse(
          context = context,
          error = it.error()
        )
      )
    )
}
