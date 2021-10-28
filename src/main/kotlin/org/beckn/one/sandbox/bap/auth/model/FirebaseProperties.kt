package org.beckn.one.sandbox.bap.auth.model


data class FirebaseProperties(
  var sessionExpiryInDays: Int = 0,
  var databaseUrl: String? = null,
  var enableStrictServerSession: Boolean = false,
  var enableCheckSessionRevoked: Boolean = false,
  var enableLogoutEverywhere: Boolean = false
)