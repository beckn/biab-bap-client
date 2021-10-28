package org.beckn.one.sandbox.bap.client.accounts.address.controllers

import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.shared.dtos.DeliveryAddressRequestDto
import org.beckn.one.sandbox.bap.client.shared.dtos.DeliveryAddressResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.AddDeliveryAddressDao
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.*
import org.litote.kmongo.newId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AddressController @Autowired constructor(
  private val responseStorageService: ResponseStorageService<DeliveryAddressResponse, AddDeliveryAddressDao>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/delivery_address")
  @ResponseBody
  fun deliveryAddress(@RequestBody request: DeliveryAddressRequestDto): ResponseEntity<DeliveryAddressResponse> {
    val user = SecurityUtil.getSecuredUserDetail()
    if (user == null) {
      return mapToErrorResponse(BppError.AuthenticationError)
    } else {
      val deliveryAddressDao = AddDeliveryAddressDao(
        userId = user?.uid,
        id = newId<String>().toString(),
        descriptor = request.descriptor,
        gps = request.gps,
        default = request.default,
        address = request.address
      )
      return responseStorageService
        .save(deliveryAddressDao)
        .fold(
          {
            log.error("Error when saving address response by user id. Error: {}", it)
            ResponseEntity
              .status(it.status())
              .body(
                DeliveryAddressResponse(
                  id = null, userId = null, context = null,
                  error = ProtocolError(code = it.status().name, message = it.message().toString())
                )
              )
          },
          {
            log.info("Saved address details of user {}")
            ResponseEntity.ok(it)
          }
        )
    }
  }

  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
      DeliveryAddressResponse(
        userId = null,
        context = null,
        error = it.error(),
        id = null
      )
    )
}