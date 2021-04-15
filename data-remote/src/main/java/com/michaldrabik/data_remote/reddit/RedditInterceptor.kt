package com.michaldrabik.data_remote.reddit

import com.michaldrabik.data_remote.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class RedditInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header(
        "User-Agent",
        "android:com.michaldrabik.showly2:v${BuildConfig.VER_NAME} (by /u/drabred)"
      )
      .build()

    return chain.proceed(request)
  }
}
