package com.michaldrabik.ui_base

import okhttp3.RequestBody
import okhttp3.internal.closeQuietly
import okio.Buffer
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

object Logger {

  fun record(
    error: Throwable,
    source: String,
  ) {
    if (error is CancellationException) return
    if (error is IOException && error.message == "Canceled" || error.cause?.message == "Canceled") return
    if (error is UnknownHostException) return
    if (error is HttpException) {
      recordHttpError(error, source)
      return
    }
  }

  private fun recordHttpError(
    error: HttpException,
    source: String,
  ) {
    val params = mutableListOf("Source" to source)

    val responseString = error.response()?.raw()?.toString() ?: ""
    val requestString = error.response()?.raw()?.request?.toString() ?: ""
    val requestBody = error.response()?.raw()?.request?.body?.asString()
    val token = requestString.substringAfter("Bearer").trim().substringBefore("]")

    params.add("Response" to responseString)
    params.add("Request" to requestString.replace(token, "***"))
    if (requestBody != null) {
      params.add("RequestBody" to requestBody)
    }
  }

  private fun RequestBody.asString(): String? {
    val buffer = Buffer()
    return try {
      this.writeTo(buffer)
      buffer.readUtf8()
    } catch (error: Throwable) {
      null
    } finally {
      buffer.closeQuietly()
    }
  }
}
