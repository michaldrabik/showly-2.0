package com.michaldrabik.data_remote.gcloud.interceptors

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.os.Build
import androidx.annotation.RequiresApi
import com.michaldrabik.data_remote.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GCloudAuthorizationInterceptor @Inject constructor(
  @ApplicationContext private val context: Context
) : Interceptor {

  @RequiresApi(Build.VERSION_CODES.P)
  override fun intercept(chain: Interceptor.Chain): Response {
    val packageName = context.packageName
    val signatures = getSignatures(packageName)

    val request = chain.request().newBuilder()
      .header("Content-Type", "application/json")
      .header("x-api-key", BuildConfig.CLOUD_API_KEY)
      .header("X-Android-Package", packageName)
      .header("X-Android-Cert", signatures.firstOrNull() ?: "")
      .build()
    return chain.proceed(request)
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun getSignatures(packageName: String): List<String> {
    val signatureList: List<String>
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // New signature
        val sig = context.packageManager.getPackageInfo(packageName, GET_SIGNING_CERTIFICATES).signingInfo
        signatureList = if (sig.hasMultipleSigners()) {
          // Send all with apkContentsSigners
          sig.apkContentsSigners.map {
            val digest = MessageDigest.getInstance("SHA")
            digest.update(it.toByteArray())
            digest.digest().toHexString(HexFormat.UpperCase)
          }
        } else {
          // Send one with signingCertificateHistory
          sig.signingCertificateHistory.map {
            val digest = MessageDigest.getInstance("SHA")
            digest.update(it.toByteArray())
            digest.digest().toHexString(HexFormat.UpperCase)
          }
        }
      } else {
        val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        signatureList = sig.map {
          val digest = MessageDigest.getInstance("SHA")
          digest.update(it.toByteArray())
          digest.digest().toHexString(HexFormat.UpperCase)
        }
      }

      return signatureList
    } catch (e: Exception) {
      // Handle error
    }
    return emptyList()
  }
}
