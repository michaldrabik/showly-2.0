package com.michaldrabik.network.tmdb

import com.michaldrabik.network.Config
import okhttp3.Interceptor
import okhttp3.Response

class TmdbInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${Config.TMDB_API_KEY}")
      .build()

    return chain.proceed(request)
  }
}
