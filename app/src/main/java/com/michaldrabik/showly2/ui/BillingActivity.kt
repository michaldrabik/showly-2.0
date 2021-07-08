package com.michaldrabik.showly2.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.queryPurchasesAsync
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.PREMIUM_LIFETIME_INAPP
import com.michaldrabik.common.Config.PREMIUM_LIFETIME_INAPP_PROMO
import com.michaldrabik.common.Config.PREMIUM_MONTHLY_SUBSCRIPTION
import com.michaldrabik.common.Config.PREMIUM_YEARLY_SUBSCRIPTION
import com.michaldrabik.showly2.App
import org.json.JSONObject
import timber.log.Timber

abstract class BillingActivity : UpdateActivity() {

  private val billingClient: BillingClient by lazy {
    BillingClient.newBuilder(applicationContext)
      .setListener { _, _ -> }
      .enablePendingPurchases()
      .build()
  }

  private val settingsRepository by lazy { (applicationContext as App).settingsRepository }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!Config.SHOW_PREMIUM || !settingsRepository.isPremium) return

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          checkOwnedPurchases()
        }
      }

      override fun onBillingServiceDisconnected() {
        Timber.w("BillingClient Disconnected")
      }
    })
  }

  override fun onDestroy() {
    billingClient.endConnection()
    super.onDestroy()
  }

  private fun checkOwnedPurchases() {
    Timber.d("Checking subscriptions...")
    lifecycleScope.launchWhenCreated {
      try {
        val subscriptions = billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        val inApps = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val purchases = subscriptions.purchasesList + inApps.purchasesList
        val eligibleProducts = mutableListOf(PREMIUM_MONTHLY_SUBSCRIPTION, PREMIUM_YEARLY_SUBSCRIPTION, PREMIUM_LIFETIME_INAPP)

        if (Config.PROMOS_ENABLED) {
          eligibleProducts.add(PREMIUM_LIFETIME_INAPP_PROMO)
        }

        if (purchases.none {
          val json = JSONObject(it.originalJson)
          val productId = json.optString("productId", "")
          it.isAcknowledged && productId in eligibleProducts
        }
        ) {
          Timber.d("No subscription found. Revoking...")
          settingsRepository.revokePremium()
          try {
            ProcessPhoenix.triggerRebirth(applicationContext)
          } catch (error: Throwable) {
            Runtime.getRuntime().exit(0)
          }
        } else {
          Timber.d("Eligible for premium!")
          billingClient.endConnection()
        }
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }
}
