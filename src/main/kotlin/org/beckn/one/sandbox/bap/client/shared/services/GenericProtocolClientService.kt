package org.beckn.one.sandbox.bap.client.shared.services

import org.beckn.one.sandbox.bap.client.external.hasBody
import org.beckn.one.sandbox.bap.client.external.isInternalServerError
import org.beckn.one.sandbox.bap.client.external.rightIf
import org.beckn.one.sandbox.bap.client.shared.errors.bap.ProtocolClientError
import org.beckn.protocol.schemas.ProtocolResponse
import org.springframework.stereotype.Service
import retrofit2.Call

@Service
class GenericProtocolClientService<Proto : ProtocolResponse> {

  fun getResponse(call: Call<List<Proto>>) =
    call.execute()
      .rightIf { it.isSuccessful && it.hasBody() }
      .bimap(
        rightOperation = { it.body().orEmpty() },
        leftOperation = {
          when {
            it.isInternalServerError() -> ProtocolClientError.Internal
            !it.hasBody() -> ProtocolClientError.NullResponse
            else -> ProtocolClientError.Internal
          }
        }
      )

}