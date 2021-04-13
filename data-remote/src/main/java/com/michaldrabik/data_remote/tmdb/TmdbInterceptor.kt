package com.michaldrabik.data_remote.tmdb

import com.michaldrabik.data_remote.Config
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
