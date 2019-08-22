package com.michaldrabik.network.trakt

import com.michaldrabik.network.Config
import okhttp3.Interceptor
import okhttp3.Response

class TraktInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("trakt-api-key", Config.TRAKT_CLIENT_ID)
      .header("trakt-api-version", Config.TRAKT_VERSION)
      .build()
    return chain.proceed(request)
  }
}