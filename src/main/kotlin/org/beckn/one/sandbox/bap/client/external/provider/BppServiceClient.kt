package org.beckn.one.sandbox.bap.client.external.provider

import org.beckn.protocol.schemas.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BppClient {
  @POST("search")
  fun search(@Body request: ProtocolSearchRequest): Call<ProtocolAckResponse>

  @POST("select")
  fun select(@Body request: ProtocolSelectRequest): Call<ProtocolAckResponse>

  @POST("init")
  fun init(@Body request: ProtocolInitRequest): Call<ProtocolAckResponse>

  @POST("confirm")
  fun confirm(@Body request: ProtocolConfirmRequest): Call<ProtocolAckResponse>

  @POST("track")
  fun track(@Body request: ProtocolTrackRequest): Call<ProtocolAckResponse>

  @POST("support")
  fun support(@Body request: ProtocolSupportRequest): Call<ProtocolAckResponse>

}
