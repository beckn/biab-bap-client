package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.Default

data class AccountRequestDto @Default constructor(
  val userName: String,
  val userPhone: String,
  val userEmail: String? = "",
)