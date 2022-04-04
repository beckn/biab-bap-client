package org.beckn.one.sandbox.bap.configurations


import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.beckn.one.sandbox.bap.auth.model.SecurityProperties
import org.beckn.one.sandbox.bap.auth.utils.JwtRequestFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.sql.Timestamp
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Autowired
    var objectMapper: ObjectMapper? = null

    @Autowired
    var restSecProps: SecurityProperties? = null



    @Autowired
    var tokenAuthenticationFilter: JwtRequestFilter? = null
    @Bean
    fun restAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint {
            httpServletRequest: HttpServletRequest?,
            httpServletResponse: HttpServletResponse,
            e: AuthenticationException? ->
            val errorObject: MutableMap<String, Any> = HashMap()
            val errorCode = 401
            errorObject["message"] = "Unauthorized access of protected resource, invalid credentials"
            errorObject["error"] = HttpStatus.UNAUTHORIZED
            errorObject["code"] = errorCode
            errorObject["timestamp"] = Timestamp(Date().time)
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = errorCode
            httpServletResponse.writer.write(objectMapper!!.writeValueAsString(errorObject))
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = restSecProps!!.allowedMethods
        configuration.allowedHeaders = restSecProps!!.allowedHeaders
        configuration.allowCredentials = restSecProps!!.allowCredentials
        configuration.exposedHeaders = restSecProps!!.exposedHeaders
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().configurationSource(corsConfigurationSource()).and().csrf().disable().formLogin().disable()
            .httpBasic().disable().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint())
            .and().authorizeRequests()
            .antMatchers("/client/v1/").permitAll()
            .antMatchers(*restSecProps?.protectedActions!!.toTypedArray()).authenticated()
            .and()
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}