package com.michaldrabik.data_remote.trakt

import android.content.SharedPreferences
import com.michaldrabik.data_remote.trakt.api.TraktApi
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

class TraktAuthenticator @Inject constructor(
  @Named("networkPreferences") private val sharedPreferences: SharedPreferences,
  private val traktApi: TraktApi
) : Authenticator {

  override fun authenticate(route: Route?, response: Response): Request? {
    val accessToken = sharedPreferences.getString("TRAKT_ACCESS_TOKEN", null)
    if (!isRequestAuthorized(response) || accessToken == null) {
      return null
    }
    synchronized(this) {
      val newAccessToken = sharedPreferences.getString("TRAKT_ACCESS_TOKEN", null)
      if (newAccessToken != accessToken) {
        return response.request.newBuilder()
          .header("Authorization", "Bearer $newAccessToken")
          .build()
      }
    }
    return null
  }

  private fun isRequestAuthorized(response: Response): Boolean {
    val header = response.request.header("Authorization")
    return header != null && header.startsWith("Bearer")
  }
}
