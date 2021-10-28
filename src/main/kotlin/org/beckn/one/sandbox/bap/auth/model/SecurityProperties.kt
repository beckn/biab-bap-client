package org.beckn.one.sandbox.bap.auth.model

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import org.beckn.one.sandbox.bap.auth.model.CookieProperties
import org.beckn.one.sandbox.bap.auth.model.FirebaseProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("security")
data class SecurityProperties(
  var cookieProps: CookieProperties? = null,
  var firebaseProps: FirebaseProperties? = null,
  var allowCredentials: Boolean = false,
  var allowedOrigins: List<String>? = null,
  var allowedHeaders: List<String>? = null,
  var exposedHeaders: List<String>? = null,
  var allowedMethods: List<String>? = null,
  var allowedPublicApis: List<String>? = null,
  var protectedActions: List<String>? = null
)