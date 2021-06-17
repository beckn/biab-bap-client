package org.beckn.one.sandbox.bap.services

import arrow.core.flatMap
import org.beckn.one.sandbox.bap.dtos.Context
import org.beckn.one.sandbox.bap.dtos.Response
import org.beckn.one.sandbox.bap.dtos.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SearchService(
  @Autowired val registryService: RegistryService,
  @Autowired val gatewayService: GatewayService
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun search(context: Context, queryString: String): ResponseEntity<Response> {
    return registryService
      .lookupGateways()
      .flatMap { gatewayService.search(it.first(), queryString) }
      .fold(
        {
          log.error("Error during search. Error: {}", it)
          ResponseEntity
            .status(it.status().value())
            .body(Response(context, it.message(), it.error()))
        },
        {
          log.info("Successfully initiated search: {}", it)
          ResponseEntity.ok(Response(context, ResponseMessage.ack()))
        }
      )
  }
}