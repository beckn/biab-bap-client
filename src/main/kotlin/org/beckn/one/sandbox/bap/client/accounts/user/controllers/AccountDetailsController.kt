package org.beckn.one.sandbox.bap.client.accounts.user.controllers

import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.accounts.user.services.AccountDetailsServices
import org.beckn.one.sandbox.bap.client.shared.dtos.AccountDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.AccountRequestDto
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.AccountDetailsDao
import org.beckn.one.sandbox.bap.message.entities.BecknResponseDao
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.beckn.protocol.schemas.*
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountDetailsController @Autowired constructor(
  private val accountDetailsServices: AccountDetailsServices,
  private val responseStorageService: ResponseStorageService<AccountDetailsResponse,AccountDetailsDao>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/account_details")
  @ResponseBody
  fun updateAccountDetails(@RequestBody request: AccountRequestDto): ResponseEntity<AccountDetailsResponse> {
    val user = SecurityUtil.getSecuredUserDetail()
    return accountDetailsServices.updateAccountDetails(user,request)
  }
}