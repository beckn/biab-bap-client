package org.beckn.one.sandbox.bap.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import io.github.resilience4j.retrofit.RetryCallAdapter
import io.github.resilience4j.retry.Retry
import okhttp3.OkHttpClient
import org.beckn.one.sandbox.bap.client.external.registry.RegistryClient
import org.beckn.one.sandbox.bap.client.shared.Util
import org.beckn.one.sandbox.bap.client.shared.security.SignRequestInterceptor
import org.beckn.one.sandbox.bap.factories.CircuitBreakerFactory
import org.beckn.one.sandbox.bap.factories.RetryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


@Configuration
class RegistryClientConfiguration(
  @Autowired @Value("\${registry_service.url}")
  private val registryServiceUrl: String,
  @Value("\${registry_service.retry.max_attempts}")
  private val maxAttempts: Int,
  @Value("\${registry_service.retry.initial_interval_in_millis}")
  private val initialIntervalInMillis: Long,
  @Value("\${registry_service.retry.interval_multiplier}")
  private val intervalMultiplier: Double,
  @Autowired @Value("\${bpp_registry_service.url}")
  private val bppRegistryServiceUrl: String,
  @Value("\${beckn.security.enabled}") private val enableSecurity: Boolean,
  @Value("\${registry_service.timeouts.connection_in_seconds}") private val connectionTimeoutInSeconds: Long,
  @Value("\${registry_service.timeouts.read_in_seconds}") private val readTimeoutInSeconds: Long,
  @Value("\${registry_service.timeouts.write_in_seconds}") private val writeTimeoutInSeconds: Long,

  @Autowired
  private val objectMapper: ObjectMapper,
  @Autowired
  private val interceptor: SignRequestInterceptor
) {

  @Bean
  @Primary
  fun registryServiceClient(): RegistryClient {
    val url : String = Util.getBaseUri(registryServiceUrl)
    val retrofit = Retrofit.Builder()
      .baseUrl(url)
      .client(buildHttpClient())
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(getRetryConfig("RegistryClient")))
      .addCallAdapterFactory(CircuitBreakerCallAdapter.of(CircuitBreakerFactory.create("RegistryClient")))
      .build()
    return retrofit.create(RegistryClient::class.java)
  }

  @Bean(BPP_REGISTRY_SERVICE_CLIENT)
  fun bppRegistryServiceClient(): RegistryClient {
    val url : String = Util.getBaseUri(bppRegistryServiceUrl)
    val retrofit = Retrofit.Builder()
      .baseUrl(url)
      .client(buildHttpClient())
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(getRetryConfig("BppRegistryClient")))
      .addCallAdapterFactory(CircuitBreakerCallAdapter.of(CircuitBreakerFactory.create("BppRegistryClient")))
      .build()

    return retrofit.create(RegistryClient::class.java)
  }

  private fun buildHttpClient(): OkHttpClient {
    val httpClientBuilder = OkHttpClient.Builder()
      .connectTimeout(connectionTimeoutInSeconds, TimeUnit.SECONDS)
      .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
      .writeTimeout(writeTimeoutInSeconds, TimeUnit.SECONDS)
    if (enableSecurity) {
      httpClientBuilder.addInterceptor(interceptor)
    }
    return httpClientBuilder.build()
  }

  private fun getRetryConfig(name: String): Retry {
    return RetryFactory.create(
      name,
      maxAttempts,
      initialIntervalInMillis,
      intervalMultiplier
    )
  }

  companion object {
    const val BPP_REGISTRY_SERVICE_CLIENT = "bppRegistryServiceClient"

  }
}