package com.michaldrabik.data_remote.trakt

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.token.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktInterceptor @Inject constructor(
  private val tokenProvider: TokenProvider,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("trakt-api-key", Config.TRAKT_CLIENT_ID)
      .header("trakt-api-version", Config.TRAKT_VERSION)
      .also { request ->
        tokenProvider.getToken()?.let {
          request.header("Authorization", "Bearer $it")
        }
      }
      .build()
    return chain.proceed(request)
  }
}
