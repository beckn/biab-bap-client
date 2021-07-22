package org.beckn.one.sandbox.bap.client.discovery.controllers

import org.beckn.one.sandbox.bap.client.discovery.services.SearchService
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.schemas.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController @Autowired constructor(
  val searchService: SearchService,
  val contextFactory: ContextFactory
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @RequestMapping("/client/v1/search")
  @ResponseBody
  fun searchV1(
    @RequestParam(required = false) searchString: String?,
    @RequestParam(required = false) location: String?,
    @RequestParam(required = false) bppId: String?,
    @RequestParam(required = false) providerId: String?,
    @RequestParam(required = false) categoryId: String?
  ): ResponseEntity<ProtocolAckResponse> {
    val context = contextFactory.create()
    val searchCriteria = SearchCriteria(searchString, location, providerId, categoryId, bppId)

    return searchService.search(context, searchCriteria)
      .fold(
        {
          log.error("Error during search. Error: {}", it)
          ResponseEntity
            .status(it.status().value())
            .body(ProtocolAckResponse(context, it.message(), it.error()))
        },
        {
          log.info("Successfully initiated Search")
          ResponseEntity.ok(ProtocolAckResponse(context, ResponseMessage.ack()))
        }
      )
  }
}