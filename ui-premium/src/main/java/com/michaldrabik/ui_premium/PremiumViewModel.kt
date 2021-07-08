package com.michaldrabik.ui_premium

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.querySkuDetails
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.PREMIUM_LIFETIME_INAPP
import com.michaldrabik.common.Config.PREMIUM_LIFETIME_INAPP_PROMO
import com.michaldrabik.common.Config.PREMIUM_MONTHLY_SUBSCRIPTION
import com.michaldrabik.common.Config.PREMIUM_YEARLY_SUBSCRIPTION
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : BaseViewModel<PremiumUiModel>() {

  private var connectionsCount = 0

  fun loadBilling(billingClient: BillingClient) {
    uiState = PremiumUiModel(isLoading = true)
    connectionsCount += 1
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.d("BillingClient Setup Finished ${billingResult.debugMessage}")
        if (billingResult.responseCode == BillingResponseCode.OK) {
          val featureResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
          if (featureResult.responseCode == BillingResponseCode.OK) {
            checkOwnedPurchases(billingClient)
            connectionsCount = 0
          } else {
            _messageLiveData.value = MessageEvent.error(R.string.errorSubscriptionsNotAvailable)
          }
        } else {
          _messageLiveData.value = MessageEvent.error(R.string.errorSubscriptionsNotAvailable)
        }
      }

      override fun onBillingServiceDisconnected() {
        if (connectionsCount > 3) {
          Timber.e("BillingClient Disconnected. All retries failed.")
          _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
          connectionsCount = 0
        } else {
          Timber.w("BillingClient Disconnected. Retrying....")
          loadBilling(billingClient)
        }
      }
    })
  }

  private fun checkOwnedPurchases(billingClient: BillingClient) {
    Timber.d("checkOwnedPurchases")
    viewModelScope.launch {
      uiState = PremiumUiModel(isLoading = true)

      try {
        val subscriptions = billingClient.queryPurchasesAsync(SkuType.SUBS)
        val inApps = billingClient.queryPurchasesAsync(SkuType.INAPP)
        val purchases = subscriptions.purchasesList + inApps.purchasesList
        val eligibleProducts = mutableListOf(PREMIUM_MONTHLY_SUBSCRIPTION, PREMIUM_YEARLY_SUBSCRIPTION, PREMIUM_LIFETIME_INAPP)
        if (Config.PROMOS_ENABLED) {
          eligibleProducts.add(PREMIUM_LIFETIME_INAPP_PROMO)
        }

        if (purchases.any {
          val json = JSONObject(it.originalJson)
          val productId = json.optString("productId", "")
          it.isAcknowledged && productId in eligibleProducts
        }
        ) {
          settingsRepository.isPremium = true
          _messageLiveData.value = MessageEvent.info(R.string.textPurchaseThanks)
          uiState = PremiumUiModel(finishEvent = ActionEvent(true))
        } else {
          loadPurchases(billingClient)
        }
      } catch (error: Throwable) {
        Timber.e(error)
        uiState = PremiumUiModel(purchaseItems = emptyList(), isLoading = false)
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun handlePurchase(
    billingClient: BillingClient,
    billingResult: BillingResult,
    purchases: MutableList<Purchase>,
  ) {
    viewModelScope.launch {
      uiState = PremiumUiModel(isLoading = true)
      Timber.d("${billingResult.responseCode} ${purchases.size}")
      when (billingResult.responseCode) {
        BillingResponseCode.OK -> {
          if (purchases.isNotEmpty()) {
            val purchase = purchases.first()
            if (purchase.purchaseState == PURCHASED && !purchase.isAcknowledged) {
              val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
              billingClient.acknowledgePurchase(params.build())
              settingsRepository.isPremium = true
              _messageLiveData.value = MessageEvent.info(R.string.textPurchaseThanks)
              uiState = PremiumUiModel(finishEvent = ActionEvent(true))
            }
          }
        }
        BillingResponseCode.ITEM_ALREADY_OWNED -> {
          settingsRepository.isPremium = true
          _messageLiveData.value = MessageEvent.info(R.string.textPurchaseThanks)
          uiState = PremiumUiModel(finishEvent = ActionEvent(true))
        }
        BillingResponseCode.USER_CANCELED -> {
          uiState = PremiumUiModel(isLoading = false)
        }
        else -> {
          uiState = PremiumUiModel(isLoading = false)
        }
      }
    }
  }

  private fun loadPurchases(billingClient: BillingClient) {
    viewModelScope.launch {
      try {
        uiState = PremiumUiModel(isLoading = true)

        val inAppsEnabled = Firebase.remoteConfig.getBoolean("in_app_enabled")

        val paramsSubs = SkuDetailsParams.newBuilder()
          .setSkusList(listOf(PREMIUM_MONTHLY_SUBSCRIPTION, PREMIUM_YEARLY_SUBSCRIPTION))
          .setType(SkuType.SUBS)
          .build()

        val paramsInApps = SkuDetailsParams.newBuilder()
          .setSkusList(listOf(PREMIUM_LIFETIME_INAPP))
          .setType(SkuType.INAPP)
          .build()

        val subsDetails = billingClient.querySkuDetails(paramsSubs)
        val inAppsDetails = billingClient.querySkuDetails(paramsInApps)

        val subsItems = subsDetails.skuDetailsList ?: emptyList()
        val inAppsItems = if (inAppsEnabled) inAppsDetails.skuDetailsList ?: emptyList() else emptyList()
        uiState = PremiumUiModel(purchaseItems = subsItems + inAppsItems, isLoading = false)
      } catch (error: Throwable) {
        Timber.e(error)
        uiState = PremiumUiModel(purchaseItems = emptyList(), isLoading = false)
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }
}
