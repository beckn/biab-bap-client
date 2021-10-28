package org.beckn.one.sandbox.bap.auth.utils

import org.beckn.one.sandbox.bap.auth.model.User
import org.springframework.security.core.context.SecurityContextHolder
import java.util.regex.Matcher
import java.util.regex.Pattern

object SecurityUtil {

  fun getSecuredUserDetail(): User? {
    val authentication = SecurityContextHolder.getContext().authentication
    return if (authentication.isAuthenticated) {
      return try {
        authentication.principal as User
      } catch (exception: Exception) {
        null
      }
    } else {
      null
    }
  }

 fun emailValidation(email: String) : Boolean {
   val pattern: Pattern = Pattern.compile(".+@.+\\.[a-z]+")
   val matcher: Matcher = pattern.matcher(email)
   return matcher.matches()
 }

}