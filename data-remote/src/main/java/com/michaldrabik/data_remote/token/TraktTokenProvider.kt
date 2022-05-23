package com.michaldrabik.data_remote.token

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@SuppressLint("ApplySharedPref")
internal class TraktTokenProvider(
  private val sharedPreferences: SharedPreferences,
  private val moshi: Moshi
) : TokenProvider {

  companion object {
    private const val KEY_ACCESS_TOKEN = "TRAKT_ACCESS_TOKEN"
    private const val KEY_REFRESH_TOKEN = "TRAKT_REFRESH_TOKEN"
  }

  private var token: String? = null

  override fun getToken(): String? {
    if (token == null) {
      token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    return token
  }

  override fun saveTokens(accessToken: String, refreshToken: String) {
    sharedPreferences.edit()
      .putString(KEY_ACCESS_TOKEN, accessToken)
      .putString(KEY_REFRESH_TOKEN, refreshToken)
      .commit()
    token = null
  }

  override fun revokeToken() {
    sharedPreferences.edit()
      .remove(KEY_ACCESS_TOKEN)
      .remove(KEY_REFRESH_TOKEN)
      .commit()
    token = null
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun refreshToken(httpClient: OkHttpClient): OAuthResponse {
    val refreshToken = sharedPreferences.getString("TRAKT_REFRESH_TOKEN", null)
      ?: throw Throwable("Refresh token is not available")

    val body = JSONObject()
      .put("refresh_token", refreshToken)
      .put("client_id", Config.TRAKT_CLIENT_ID)
      .put("client_secret", Config.TRAKT_CLIENT_SECRET)
      .put("redirect_uri", Config.TRAKT_REDIRECT_URL)
      .put("grant_type", "refresh_token")
      .toString()

    val request = Request.Builder()
      .url("${Config.TRAKT_BASE_URL}oauth/token")
      .post(body.toRequestBody("application/json".toMediaType()))
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw Throwable("Refresh token call failed. ${response.message}")
      } else {
        val responseSource = response.body!!.source()
        return moshi.adapter(OAuthResponse::class.java).fromJson(responseSource)!!
      }
    }
  }
}
