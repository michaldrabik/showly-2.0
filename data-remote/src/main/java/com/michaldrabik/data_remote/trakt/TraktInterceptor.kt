package com.michaldrabik.data_remote.trakt

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.data_remote.Config
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

    var tryCount = 0
    var response = chain.proceed(request)

    while (response.code == 429 && tryCount < 3) {
      Timber.w("429 Too Many Requests. Retrying...")
      tryCount += 1
      Thread.sleep(3000)
      response.close()
      response = chain.proceed(request)
    }

    if (response.code == 429) {
      val url = response.request.url.toUrl().toString()
      val error = Throwable("429 Too Many Requests")
      Timber.e(error)
      FirebaseCrashlytics.getInstance().run {
        setCustomKey("URL", url)
        recordException(error)
      }
    }

    return response
  }
}
