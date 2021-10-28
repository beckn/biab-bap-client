package org.beckn.one.sandbox.bap.auth.model

import lombok.AllArgsConstructor
import com.google.firebase.auth.FirebaseToken
import lombok.Data

@Data
@AllArgsConstructor
data class Credentials(
  var type: CredentialType? = null,
  var decodedToken: FirebaseToken? = null,
  var idToken: String? = null,
  var session: String? = null,
) {
  enum class CredentialType {
    ID_TOKEN, SESSION
  }
}

