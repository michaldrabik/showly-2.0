package com.michaldrabik.data_remote.trakt.interceptors

import com.michaldrabik.data_remote.Config
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktHeadersInterceptor @Inject constructor() : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("trakt-api-key", Config.TRAKT_CLIENT_ID)
      .header("trakt-api-version", Config.TRAKT_VERSION)
      .build()
    return chain.proceed(request)
  }
}
