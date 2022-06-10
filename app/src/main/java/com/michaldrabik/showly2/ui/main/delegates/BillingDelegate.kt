package com.michaldrabik.showly2.ui.main.delegates

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.queryPurchasesAsync
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.repository.settings.SettingsRepository
import org.json.JSONObject
import timber.log.Timber

interface BillingDelegate {
  fun registerBilling(
    activity: AppCompatActivity,
    settingsRepository: SettingsRepository
  )
}

class MainBillingDelegate : BillingDelegate, DefaultLifecycleObserver {

  private lateinit var activity: AppCompatActivity
  private lateinit var settingsRepository: SettingsRepository

  private val billingClient: BillingClient by lazy {
    BillingClient.newBuilder(activity.applicationContext)
      .setListener { _, _ -> }
      .enablePendingPurchases()
      .build()
  }

  override fun registerBilling(
    activity: AppCompatActivity,
    settingsRepository: SettingsRepository
  ) {
    this.settingsRepository = settingsRepository
    this.activity = activity
    this.activity.lifecycle.addObserver(this)
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    Timber.d("onCreate")
    if (!Config.SHOW_PREMIUM || !settingsRepository.isPremium) {
      Timber.d("Premium inactive. Finishing.")
      return
    }

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

  override fun onDestroy(owner: LifecycleOwner) {
    billingClient.endConnection()
    Timber.d("onDestroy")
    super.onDestroy(owner)
  }

  private fun checkOwnedPurchases() {
    Timber.d("Checking purchases...")
    activity.lifecycleScope.launchWhenCreated {
      try {
        val subscriptions = billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        val inApps = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val purchases = subscriptions.purchasesList + inApps.purchasesList
        val eligibleProducts = mutableListOf(Config.PREMIUM_MONTHLY_SUBSCRIPTION, Config.PREMIUM_YEARLY_SUBSCRIPTION, Config.PREMIUM_LIFETIME_INAPP)

        if (Config.PROMOS_ENABLED) {
          eligibleProducts.add(Config.PREMIUM_LIFETIME_INAPP_PROMO)
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
            ProcessPhoenix.triggerRebirth(activity.applicationContext)
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
