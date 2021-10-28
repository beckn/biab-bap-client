package org.beckn.one.sandbox.bap.client.accounts.user.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.beckn.one.sandbox.bap.auth.model.User
import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.accounts.address.services.AddressServices
import org.beckn.one.sandbox.bap.client.accounts.billings.services.BillingDetailService
import org.beckn.one.sandbox.bap.client.shared.dtos.AccountDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.AccountRequestDto
import org.beckn.one.sandbox.bap.client.shared.dtos.BillingDetailsResponse
import org.beckn.one.sandbox.bap.client.shared.dtos.DeliveryAddressResponse
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.one.sandbox.bap.message.entities.AccountDetailsDao
import org.beckn.one.sandbox.bap.message.entities.BecknResponseDao
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AccountDetailsServices @Autowired constructor(
  private val addressServices: AddressServices,
  private val billingDetailService: BillingDetailService,
  private val responseStorageService: ResponseStorageService<AccountDetailsResponse, AccountDetailsDao>
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)


  fun findAccountDetailForCurrentUser(
    userId: String
  ): ResponseEntity<AccountDetailsResponse> = responseStorageService
    .findById(userId)
    .fold(
      {
        log.error("Error when finding search response by message id. Error: {}", it)
        ResponseEntity
          .status(it.status().value())
          .body(AccountDetailsResponse(userId = null, error = it.error(), context = null))
      },
      {
        return bindBillingAndAccount(it, userId)

      }
    )

  private fun bindBillingAndAccount(
    accountResponse: AccountDetailsResponse?,
    userId: String
  ): ResponseEntity<AccountDetailsResponse> {

    val addressResponse = addressServices.findAddressesForCurrentUser(userId)
    val billingResponse = billingDetailService.findBillingsForCurrentUser(userId)
    if (addressResponse.statusCodeValue == 200) {
      accountResponse?.address = addressResponse.body as List<DeliveryAddressResponse>
    }
    if (billingResponse.statusCodeValue == 200) {
      accountResponse?.billing = billingResponse.body as List<BillingDetailsResponse>
    }
    return ResponseEntity.ok(accountResponse)
  }

  fun updateAccountDetails(user: User?, request: AccountRequestDto): ResponseEntity<AccountDetailsResponse> {
    val requestBody = AccountDetailsDao(
      email = request.userEmail,
      phone = request.userPhone,
      name = request.userName,
      userId = user?.uid
    )
    try {
      return if (user != null) {
        if (!SecurityUtil.emailValidation(request.userEmail ?: "")) {
          mapToErrorResponse(BppError.BadRequestError)
        } else if (!request.userEmail.equals(user.email, ignoreCase = true)) {
          val updateRequestData =
            FirebaseAuth.getInstance().getUser(user.uid).updateRequest().setEmail(request.userEmail)
          FirebaseAuth.getInstance().updateUser(updateRequestData)
          updateAccountDetailInDb(requestBody)
        } else {
          updateAccountDetailInDb(requestBody)
        }
      } else {
        log.error("Error of authentication when updating account info}")
        mapToErrorResponse(BppError.AuthenticationError)
      }

    } catch (e: FirebaseAuthException) {
      log.error("Error when updating email in firebase. Error: {}")
      return mapToErrorResponse(BppError.BadRequestError)
    }

  }

  private fun updateAccountDetailInDb(requestBody: AccountDetailsDao): ResponseEntity<AccountDetailsResponse> {
    return responseStorageService
      .updateDocByQuery(BecknResponseDao::userId eq requestBody.userId!!, requestBody)
      .fold(
        {
          log.error("Error when saving account details response. Error: {}", it)
          mapToErrorResponse(it)
        },
        {
          log.info("Updated account details of user {}")
          ResponseEntity.ok(it)
        }
      )
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
