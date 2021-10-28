package org.beckn.one.sandbox.bap.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import io.github.resilience4j.retrofit.RetryCallAdapter
import io.github.resilience4j.retry.Retry
import okhttp3.OkHttpClient
import org.beckn.one.sandbox.bap.client.external.bap.ProtocolClient
import org.beckn.one.sandbox.bap.client.shared.Util
import org.beckn.one.sandbox.bap.factories.CircuitBreakerFactory
import org.beckn.one.sandbox.bap.factories.RetryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

@Configuration
class BapClientConfiguration(
  @Autowired @Value("\${protocol_service.url}")
  private val bapServiceUrl: String,
  @Value("\${protocol_service.retry.max_attempts}")
  private val maxAttempts: Int,
  @Value("\${protocol_service.retry.initial_interval_in_millis}")
  private val initialIntervalInMillis: Long,
  @Value("\${protocol_service.retry.interval_multiplier}")
  private val intervalMultiplier: Double,
  @Value("\${protocol_service.timeouts.connection_in_seconds}") private val connectionTimeoutInSeconds: Long,
  @Value("\${protocol_service.timeouts.read_in_seconds}") private val readTimeoutInSeconds: Long,
  @Value("\${protocol_service.timeouts.write_in_seconds}") private val writeTimeoutInSeconds: Long,

  @Autowired private val objectMapper: ObjectMapper
) {
  @Bean
  fun bapClient(): ProtocolClient {
    val url : String = Util.getBaseUri(bapServiceUrl)
    val retrofit = Retrofit.Builder()
      .baseUrl(url)
      .client(buildHttpClient())
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(getRetryConfig()))
      .addCallAdapterFactory(CircuitBreakerCallAdapter.of(CircuitBreakerFactory.create("BapClient")))
      .build()

    return retrofit.create(ProtocolClient::class.java)
  }

  private fun buildHttpClient(): OkHttpClient {
    val httpClientBuilder = OkHttpClient.Builder()
      .connectTimeout(connectionTimeoutInSeconds, TimeUnit.SECONDS)
      .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
      .writeTimeout(writeTimeoutInSeconds, TimeUnit.SECONDS)
    return httpClientBuilder.build()
  }

  private fun getRetryConfig(): Retry {
    return RetryFactory.create(
      "BapClient",
      maxAttempts,
      initialIntervalInMillis,
      intervalMultiplier
    )
  }

}