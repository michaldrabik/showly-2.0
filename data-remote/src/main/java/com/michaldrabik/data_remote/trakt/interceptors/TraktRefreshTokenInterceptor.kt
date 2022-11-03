package com.michaldrabik.data_remote.trakt.interceptors

import com.michaldrabik.data_remote.token.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktRefreshTokenInterceptor @Inject constructor(
  private val tokenProvider: TokenProvider,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
    val request = chain.request()
    if (tokenProvider.shouldRefresh()) {
      try {
        val refreshedTokens = tokenProvider.refreshToken()
        tokenProvider.saveTokens(
          accessToken = refreshedTokens.access_token,
          refreshToken = refreshedTokens.refresh_token
        )
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
    chain.proceed(request)
  }
}
