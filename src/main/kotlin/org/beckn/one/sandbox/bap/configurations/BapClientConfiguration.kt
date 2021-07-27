package org.beckn.one.sandbox.bap.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Configuration
class BapClientConfiguration(
  @Autowired @Value("\${protocol_service.url}")
  private val bapServiceUrl: String,
  @Autowired
  private val objectMapper: ObjectMapper
) {

  @Bean
  fun bapClient(): ProtocolClient {
    val retrofit = Retrofit.Builder()
      .baseUrl(bapServiceUrl)
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .build()

    return retrofit.create(ProtocolClient::class.java)
  }

}