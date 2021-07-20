package org.beckn.one.sandbox.bap.protocol.confirm.controllers

import org.beckn.one.sandbox.bap.protocol.base.controllers.PollForResponseController
import org.beckn.one.sandbox.bap.protocol.base.services.PollForResponseService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnConfirm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PollConfirmResponseController @Autowired constructor(
  responseService: PollForResponseService<ProtocolOnConfirm>,
  contextFactory: ContextFactory
): PollForResponseController<ProtocolOnConfirm>(responseService, contextFactory) {

  @RequestMapping("/v1/on_confirm")
  @ResponseBody
  fun getConfirmResponses(messageId: String) = findResponses(messageId)

}