package org.beckn.one.sandbox.bap.client.accounts.billings.services

import org.beckn.one.sandbox.bap.client.shared.dtos.BillingDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.DeliveryAddressResponse
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.BillingDetailsDao
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.ProtocolError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class BillingDetailService @Autowired constructor(
  private val responseStorageService: ResponseStorageService<BillingDetailsResponse, BillingDetailsDao>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun findBillingsForCurrentUser(
    userId: String
  ): ResponseEntity<List<BillingDetailsResponse>> = responseStorageService
    .findManyByUserId(userId,0,0)
    .fold(
      {
        log.error("Error when finding search response by message id. Error: {}", it)
        mapToErrorResponse(it)
      },
      {
        log.info("Found responses for address {}", userId)
        ResponseEntity.ok(it)
      }
    )

  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
      listOf(
        BillingDetailsResponse(
        id = null,
        name = null,
        phone = null,
        userId = null,
        context = null,
        error = it.error()
      )
      ))
}
