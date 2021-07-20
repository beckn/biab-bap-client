package org.beckn.one.sandbox.bap.protocol.select.controllers

import org.beckn.one.sandbox.bap.protocol.base.controllers.PollForResponseController
import org.beckn.one.sandbox.bap.protocol.base.services.PollForResponseService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSelect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PollSelectResponseController @Autowired constructor(
  responseService: PollForResponseService<ProtocolOnSelect>,
  contextFactory: ContextFactory
): PollForResponseController<ProtocolOnSelect>(responseService, contextFactory) {

  @RequestMapping("/v1/on_select")
  @ResponseBody
  fun getSelectResponses(messageId: String) = findResponses(messageId)

}