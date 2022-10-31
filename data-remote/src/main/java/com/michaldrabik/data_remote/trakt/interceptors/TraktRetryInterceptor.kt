package com.michaldrabik.data_remote.trakt.interceptors

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktRetryInterceptor @Inject constructor() : Interceptor {

  private val mutex = Mutex()
  private var tryCount = 0

  override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
    mutex.withLock {
      tryCount = 0
      val request = chain.request()
      var response = chain.proceed(request)

      while (response.code == 429 && tryCount < 3) {
        Timber.w("429 Too Many Requests. Retrying...")
        delay(3000)
        tryCount += 1
        response.close()
        response = chain.proceed(request)
      }

      if (response.code == 429) {
        val error = Throwable("429 Too Many Requests")
        Timber.e(error)
        Firebase.analytics.logEvent(
          "trakt_too_many_requests",
          Bundle().apply {
            putString("url", response.request.url.toString())
          }
        )
      }

      response
    }
  }
}
