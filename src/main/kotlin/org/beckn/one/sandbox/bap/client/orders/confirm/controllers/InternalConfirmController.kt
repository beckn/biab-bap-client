package org.beckn.one.sandbox.bap.client.orders.confirm.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ListWrapperResponseMessage
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InternalConfirmController @Autowired constructor(
  val confirmStore: ResponseStorageService<ProtocolOnConfirm>,
  val contextFactory: ContextFactory
) {

  @RequestMapping("/client/v1/on_confirm")
  @ResponseBody
  fun onConfirmV1(
    @RequestParam messageId: String
  ) = confirmStore.findByMessageId(messageId).fold(
    {
      ResponseEntity
        .status(it.status().value())
        .body(ClientErrorResponse(context = contextFactory.create(), error = it.error()))
    },
    {
      ResponseEntity.ok(ListWrapperResponseMessage(it))
    }
  )
}