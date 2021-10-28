package org.beckn.one.sandbox.bap.client.accounts.user.controllers

import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.accounts.user.services.AccountDetailsServices
import org.beckn.one.sandbox.bap.client.shared.dtos.AccountDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.AccountDetailsDao
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnAccountDetailsController @Autowired constructor(
  private val accountDetailsServices: AccountDetailsServices,
  private val responseRepository: ResponseStorageService<AccountDetailsResponse, AccountDetailsDao>

) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @RequestMapping("/client/v1/account_details")
  @ResponseBody
  fun onAccountDetails(): ResponseEntity<out AccountDetailsResponse> {
    val user = SecurityUtil.getSecuredUserDetail()
    return if (user != null) {
      accountDetailsServices.findAccountDetailForCurrentUser(user?.uid!!)
    } else {
      log.error("Error of authentication on getting account info}")
      mapToErrorResponse(BppError.AuthenticationError)
    }
  }

  private fun mapToErrorResponse(it: HttpError) = ResponseEntity
    .status(it.status())
    .body(
      AccountDetailsResponse(
        userId = null,
        context = null,
        error = it.error()
      )
    )
}