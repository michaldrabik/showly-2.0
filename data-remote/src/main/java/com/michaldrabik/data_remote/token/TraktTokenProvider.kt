package com.michaldrabik.data_remote.token

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.squareup.moshi.Moshi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.time.Duration
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("ApplySharedPref")
@Singleton
internal class TraktTokenProvider(
  private val sharedPreferences: SharedPreferences,
  private val moshi: Moshi,
  @Named("okHttpBase") private val okHttpClient: OkHttpClient,
) : TokenProvider {

  companion object {
    private const val KEY_ACCESS_TOKEN = "TRAKT_ACCESS_TOKEN"
    private const val KEY_REFRESH_TOKEN = "TRAKT_REFRESH_TOKEN"
    private const val KEY_TIMESTAMP = "TRAKT_ACCESS_TOKEN_TIMESTAMP"

    private val TRAKT_TOKEN_REFRESH_COOLDOWN: Duration = Duration.ofDays(1)
  }

  private var token: String? = null
  private var lastRefreshCheck: Long = 0

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
      .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
      .commit()
    token = null
  }

  override fun revokeToken() {
    sharedPreferences.edit()
      .clear()
      .remove(KEY_ACCESS_TOKEN)
      .remove(KEY_REFRESH_TOKEN)
      .remove(KEY_TIMESTAMP)
      .commit()
    token = null
  }

  override suspend fun shouldRefresh(): Boolean {
    val now = System.currentTimeMillis()
    if (lastRefreshCheck > 0L && now - lastRefreshCheck < TRAKT_TOKEN_REFRESH_COOLDOWN.toMillis()) {
      return false
    }
    lastRefreshCheck = now
    val timestamp = sharedPreferences.getLong(KEY_TIMESTAMP, 0L)
    if (timestamp == 0L) {
      return true
    }
    if (now - timestamp > Config.TRAKT_TOKEN_REFRESH_DURATION.toMillis()) {
      return true
    }
    return false
  }

  override suspend fun refreshToken(): OAuthResponse {
    val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
      ?: throw Error("Refresh token is not available")

    val body = JSONObject()
      .put("refresh_token", refreshToken)
      .put("client_id", Config.TRAKT_CLIENT_ID)
      .put("client_secret", Config.TRAKT_CLIENT_SECRET)
      .put("redirect_uri", Config.TRAKT_REDIRECT_URL)
      .put("grant_type", "refresh_token")
      .toString()

    val request = Request.Builder()
      .url("${Config.TRAKT_BASE_URL}oauth/token")
      .addHeader("Content-Type", "application/json")
      .post(body.toRequestBody("application/json".toMediaType()))
      .build()

    Timber.d("Making refresh token call...")

    return suspendCancellableCoroutine {
      val callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          Timber.d("Refresh token call failed. $e")
          it.resumeWithException(Error("Refresh token call failed. $e"))
        }

        override fun onResponse(call: Call, response: Response) {
          if (response.isSuccessful) {
            Timber.d("Refresh token success!")
            val responseSource = response.body!!.source()
            val result = moshi.adapter(OAuthResponse::class.java).fromJson(responseSource)!!
            it.resume(result)
          } else {
            it.resumeWithException(Error("Refresh token call failed. ${response.code}"))
          }
          response.closeQuietly()
        }
      }
      val call = okHttpClient.newCall(request)
      it.invokeOnCancellation { call.cancel() }
      call.enqueue(callback)
    }
  }
}
