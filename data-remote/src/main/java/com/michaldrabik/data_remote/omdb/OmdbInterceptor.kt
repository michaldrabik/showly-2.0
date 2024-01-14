package com.michaldrabik.data_remote.omdb

import com.michaldrabik.data_remote.Config
import okhttp3.Interceptor
import okhttp3.Response

class OmdbInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val url = chain.request().url.newBuilder()
      .addQueryParameter("apikey", Config.OMDB_API_KEY)
      .addQueryParameter("tomatoes", "true")
      .build()
    val request = chain.request().newBuilder()
      .url(url)
      .build()
    return chain.proceed(request)
  }
}
