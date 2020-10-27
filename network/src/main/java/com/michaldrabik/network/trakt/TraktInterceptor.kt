package com.michaldrabik.network.trakt

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.network.Config
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TraktInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("trakt-api-key", Config.TRAKT_CLIENT_ID)
      .header("trakt-api-version", Config.TRAKT_VERSION)
      .build()

    val response = chain.proceed(request)
    if (response.code == 429) {
      //Log Firebase error in case of rate limit hits start appearing.
      Timber.e("429 Too Many Requests")
      FirebaseCrashlytics.getInstance().recordException(Throwable("429 Too Many Requests"))
    }

    return response
  }
}
