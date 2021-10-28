package org.beckn.one.sandbox.bap.client.accounts.address.controllers

import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.accounts.address.services.AddressServices
import org.beckn.one.sandbox.bap.client.shared.dtos.DeliveryAddressResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class OnAddressController @Autowired constructor(
  private val addressService: AddressServices
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @RequestMapping("/client/v1/delivery_address")
  @ResponseBody
  fun onSearchAddress(): ResponseEntity<out List<DeliveryAddressResponse>> {
    val user = SecurityUtil.getSecuredUserDetail()
    return if(user != null){
       addressService.findAddressesForCurrentUser(user?.uid!!)
    }else{
      mapToErrorResponse(BppError.AuthenticationError)
    }
  }

  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
     listOf(
       DeliveryAddressResponse(
         userId = null,
         context= null,
         error = it.error(),
         id = null
       )
     )
    )
}