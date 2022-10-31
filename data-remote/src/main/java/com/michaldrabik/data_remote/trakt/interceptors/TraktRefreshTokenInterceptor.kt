package com.michaldrabik.data_remote.trakt.interceptors

import com.michaldrabik.data_remote.token.TokenProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktRefreshTokenInterceptor @Inject constructor(
  private val tokenProvider: TokenProvider,
) : Interceptor {

  private val mutex = Mutex()
  private lateinit var httpClient: OkHttpClient

  fun setHttpClient(httpClient: OkHttpClient) {
    this.httpClient = httpClient
  }

  override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
    mutex.withLock {
      val request = chain.request()
      if (tokenProvider.shouldRefresh()) {
        try {
          val refreshedTokens = tokenProvider.refreshToken(httpClient)
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
}
