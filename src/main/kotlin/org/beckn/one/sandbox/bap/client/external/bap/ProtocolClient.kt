package org.beckn.one.sandbox.bap.client.external.bap

import org.beckn.protocol.schemas.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ProtocolClient {
  @GET("protocol/v1/on_search")
  fun getSearchResponsesCall(@Query("messageId") messageId: String): Call<List<ProtocolOnSearch>>

  @GET("protocol/v1/on_select")
  fun getSelectResponsesCall(@Query("messageId") messageId: String): Call<List<ProtocolOnSelect>>

  @GET("protocol/v1/on_init")
  fun getInitResponsesCall(@Query("messageId") messageId: String): Call<List<ProtocolOnInit>>

  @GET("protocol/v1/on_confirm")
  fun getConfirmResponsesCall(@Query("messageId") messageId: String): Call<List<ProtocolOnConfirm>>

  @GET("protocol/v1/on_track")
  fun getTrackResponsesCall(@Query("messageId") messageId: String): Call<List<ProtocolOnTrack>>

  @GET("protocol/v1/on_support")
  fun getSupportResponseCall(@Query("messageId") messageId: String): Call<List<ProtocolOnSupport>>
}