package org.beckn.one.sandbox.bap.auth.service


import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest

@Service
class SecurityService {
    fun getBearerToken(request: HttpServletRequest): String? {
        var bearerToken: String? = null
        val authorization = request.getHeader("Authorization")
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7)
        }
        return bearerToken
    }
}