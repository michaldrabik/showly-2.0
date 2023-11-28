package com.michaldrabik.data_remote.gcloud.interceptors

import com.michaldrabik.data_remote.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GCloudAuthorizationInterceptor @Inject constructor() : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("x-api-key", BuildConfig.CLOUD_API_KEY)
      .build()
    return chain.proceed(request)
  }
}
