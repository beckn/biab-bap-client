package org.beckn.one.sandbox.bap.client.external.provider

import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolInitRequest
import org.beckn.protocol.schemas.ProtocolSearchRequest
import org.beckn.protocol.schemas.ProtocolSelectRequest
import org.beckn.protocol.schemas.ProtocolConfirmRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BppServiceClient {
  @POST("search")
  fun search(@Body request: ProtocolSearchRequest): Call<ProtocolAckResponse>

  @POST("select")
  fun select(@Body request: ProtocolSelectRequest): Call<ProtocolAckResponse>

  @POST("init")
  fun init(@Body request: ProtocolInitRequest): Call<ProtocolAckResponse>

  @POST("confirm")
  fun confirm(@Body request: ProtocolConfirmRequest): Call<ProtocolAckResponse>

}
