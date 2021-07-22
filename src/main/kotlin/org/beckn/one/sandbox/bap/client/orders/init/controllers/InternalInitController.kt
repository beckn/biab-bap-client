package org.beckn.one.sandbox.bap.client.orders.init.controllers

import org.beckn.one.sandbox.bap.client.shared.dtos.ClientErrorResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.ListWrapperResponseMessage
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnInit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InternalInitController @Autowired constructor(
  val initStore: ResponseStorageService<ProtocolOnInit>,
  val contextFactory: ContextFactory
) {

  @RequestMapping("/client/v1/on_init")
  @ResponseBody
  fun onInitializeV1(
    @RequestParam messageId: String
  ) = initStore.findByMessageId(messageId)
    .fold(
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