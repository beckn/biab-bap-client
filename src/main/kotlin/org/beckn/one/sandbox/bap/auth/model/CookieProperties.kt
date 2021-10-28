package org.beckn.one.sandbox.bap.auth.model

data class CookieProperties(
  var domain: String,
  var path: String,
  var httpOnly: Boolean = false,
  var secure: Boolean = false,
  var maxAgeInMinutes: Int = 0
)