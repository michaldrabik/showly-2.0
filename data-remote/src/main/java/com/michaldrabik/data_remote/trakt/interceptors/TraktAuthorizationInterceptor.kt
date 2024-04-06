package com.michaldrabik.data_remote.trakt.interceptors

import com.michaldrabik.data_remote.token.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthorizationInterceptor @Inject constructor(
  private val tokenProvider: TokenProvider,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
    tokenProvider.getToken()?.let {
      request.addHeader("Authorization", "Bearer $it")
    }
    return chain.proceed(request.build())
  }
}
