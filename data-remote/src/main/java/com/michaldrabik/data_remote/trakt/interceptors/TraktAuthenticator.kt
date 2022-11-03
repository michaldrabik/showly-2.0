package com.michaldrabik.data_remote.trakt.interceptors

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.data_remote.token.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthenticator @Inject constructor(
  private val tokenProvider: TokenProvider,
) : Authenticator {

  override fun authenticate(route: Route?, response: Response): Request? =
    runBlocking {
      val accessToken = tokenProvider.getToken()
      if (!isRequestAuthorized(response) || accessToken == null) {
        return@runBlocking null
      }

      val newAccessToken = tokenProvider.getToken()
      if (newAccessToken != accessToken) {
        response.request.newBuilder()
          .header("Authorization", "Bearer $newAccessToken")
          .build()
      }

      try {
        Timber.d("Refreshing access token...")
        val refreshedTokens = tokenProvider.refreshToken()

        tokenProvider.saveTokens(
          accessToken = refreshedTokens.access_token,
          refreshToken = refreshedTokens.refresh_token
        )

        response.request
          .newBuilder()
          .header("Authorization", "Bearer ${refreshedTokens.access_token}")
          .build()
      } catch (error: Throwable) {
        Timber.d(error)
        FirebaseCrashlytics.getInstance().run { recordException(error) }
        null
      }
    }

  private fun isRequestAuthorized(response: Response): Boolean {
    val header = response.request.header("Authorization")
    return header != null && header.startsWith("Bearer")
  }
}
