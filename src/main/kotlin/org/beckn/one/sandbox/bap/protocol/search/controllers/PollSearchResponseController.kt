package org.beckn.one.sandbox.bap.protocol.search.controllers

import org.beckn.one.sandbox.bap.protocol.base.controllers.PollForResponseController
import org.beckn.one.sandbox.bap.protocol.base.services.PollForResponseService
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolOnSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PollSearchResponseController @Autowired constructor(
  responseService: PollForResponseService<ProtocolOnSearch>,
  contextFactory: ContextFactory
): PollForResponseController<ProtocolOnSearch>(responseService, contextFactory) {

  @RequestMapping("/v1/on_search")
  @ResponseBody
  fun getSearchResponses(messageId: String) = findResponses(messageId)

}