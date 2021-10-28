package org.beckn.one.sandbox.bap.auth.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import org.beckn.one.sandbox.bap.auth.model.Credentials
import org.beckn.one.sandbox.bap.auth.model.SecurityProperties
import org.beckn.one.sandbox.bap.auth.model.User
import org.beckn.one.sandbox.bap.auth.service.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class JwtRequestFilter : OncePerRequestFilter() {
  @Autowired
  var securityService: SecurityService? = null

  @Autowired
  var securityProps: SecurityProperties? = null




  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    verifyToken(request)
    filterChain.doFilter(request, response)
  }

    fun verifyToken(request: HttpServletRequest) {
    var session: String? = null
    var decodedToken: FirebaseToken? = null
    var type: Credentials.CredentialType? = null
    val strictServerSessionEnabled: Boolean = securityProps?.firebaseProps!!.enableStrictServerSession
    val token: String? = securityService?.getBearerToken(request)
    logger.info(token)
    try {
    if (!strictServerSessionEnabled) {
        if (token != null && !token.equals("undefined", ignoreCase = true)) {
          decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
          type = Credentials.CredentialType.ID_TOKEN
        }
      }
    } catch (e: FirebaseAuthException) {
      e.printStackTrace()
    }
    val user: User? = firebaseTokenToUserDto(decodedToken)
    if (user != null) {

      val authentication = UsernamePasswordAuthenticationToken(
        user,
        Credentials(type, decodedToken, token, session), null
      )

      authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
      SecurityContextHolder.getContext().authentication = authentication
    }
  }

  fun firebaseTokenToUserDto(decodedToken: FirebaseToken?): User? {
    var user: User? = null
    if (decodedToken != null) {
      user = User()
      user.uid = decodedToken.uid
      user.name = decodedToken.name
      user.email = decodedToken.email
      user.picture = decodedToken.picture
      user.issuer= decodedToken.issuer
      user.isEmailVerified = decodedToken.isEmailVerified
    }
    return user
  }

}