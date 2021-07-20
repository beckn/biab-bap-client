package org.beckn.one.sandbox.bap.protocol.init.controllers

import org.beckn.one.sandbox.bap.protocol.base.controllers.PollForResponseController
import org.beckn.one.sandbox.bap.protocol.base.services.PollForResponseService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnInit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PollInitResponseController @Autowired constructor(
  responseService: PollForResponseService<ProtocolOnInit>,
  contextFactory: ContextFactory
): PollForResponseController<ProtocolOnInit>(responseService, contextFactory) {

  @RequestMapping("/v1/on_init")
  @ResponseBody
  fun getInitResponses(messageId: String) = findResponses(messageId)

}