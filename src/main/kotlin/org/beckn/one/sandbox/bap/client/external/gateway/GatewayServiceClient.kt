package org.beckn.one.sandbox.bap.client.external.gateway

import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolSearchRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GatewayClient {
  @POST("search")
  fun search(@Body request: ProtocolSearchRequest): Call<ProtocolAckResponse>
}