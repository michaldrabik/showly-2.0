package com.michaldrabik.data_remote.trakt

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.michaldrabik.data_remote.token.TokenProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthenticator @Inject constructor(
  private val tokenProvider: TokenProvider
) : Authenticator {

  private val mutex = Mutex()
  private lateinit var httpClient: OkHttpClient

  fun setHttpClient(httpClient: OkHttpClient) {
    this.httpClient = httpClient
  }

  override fun authenticate(route: Route?, response: Response): Request? =
    runBlocking {
      val accessToken = tokenProvider.getToken()
      if (!isRequestAuthorized(response) || accessToken == null) {
        return@runBlocking null
      }

      mutex.withLock {
        val newAccessToken = tokenProvider.getToken()
        if (newAccessToken != accessToken) {
          response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
        }

        return@runBlocking try {
          Timber.d("Refreshing access token...")
          val refreshedTokens = tokenProvider.refreshToken(httpClient)

          tokenProvider.saveTokens(
            accessToken = refreshedTokens.access_token,
            refreshToken = refreshedTokens.refresh_token
          )

          Firebase.analytics.logEvent(
            "trakt_token_refresh",
            Bundle().apply {
              putString("url", response.request.url.toString())
            }
          )

          response.request
            .newBuilder()
            .header("Authorization", "Bearer ${refreshedTokens.access_token}")
            .build()
        } catch (error: Throwable) {
          Timber.d(error)
          Firebase.analytics.logEvent(
            "trakt_token_refresh_error",
            Bundle().apply {
              putString("url", response.request.url.toString())
              putString("error", error.message)
            }
          )
          null
        }
      }
    }

  private fun isRequestAuthorized(response: Response): Boolean {
    val header = response.request.header("Authorization")
    return header != null && header.startsWith("Bearer")
  }
}
