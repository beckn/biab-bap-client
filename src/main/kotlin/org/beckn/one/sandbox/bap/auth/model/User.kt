package org.beckn.one.sandbox.bap.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
  var uid: String? = null,
  var name: String? = null,
  var email: String? = null,
  var isEmailVerified: Boolean = false,
  var issuer: String? = null,
  var picture: String? = null
) {
  companion object {
    private const val serialVersionUID = 4408418647685225829L
  }
}