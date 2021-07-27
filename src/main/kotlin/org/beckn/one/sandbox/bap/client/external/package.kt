package org.beckn.one.sandbox.bap.client.external

import arrow.core.Either
import org.springframework.http.HttpStatus
import retrofit2.Response
import java.util.function.Predicate


fun <T> Response<T>.rightIf(cond: Predicate<Response<T>>) =
  if (cond.test(this)) Either.Right(this) else Either.Left(this)

fun <T> Response<T>.isInternalServerError() = this.code() == HttpStatus.INTERNAL_SERVER_ERROR.value()

fun <T> Response<T>.hasBody() = this.body() != null