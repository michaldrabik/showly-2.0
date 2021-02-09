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
import com.android.billingclient.api.querySkuDetails
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_repository.SettingsRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class PremiumViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : BaseViewModel<PremiumUiModel>() {

  companion object {
    private const val MONTHLY_SUBSCRIPTION = "showly_premium_1_month"
    private const val YEARLY_SUBSCRIPTION = "showly_premium_1_year"
    private const val LIFETIME_PROMO_INAPP = "showly_premium_lifetime_promo"
  }

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
        val subscriptions = billingClient.queryPurchases(SkuType.SUBS)
        val inApps = billingClient.queryPurchases(SkuType.INAPP)
        val purchases = (subscriptions.purchasesList ?: emptyList()) + (inApps.purchasesList ?: emptyList())
        val eligibleProducts = mutableListOf(MONTHLY_SUBSCRIPTION, YEARLY_SUBSCRIPTION)
        if (Config.PROMOS_ENABLED) eligibleProducts.add(LIFETIME_PROMO_INAPP)

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
    purchases: MutableList<Purchase>
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

        val paramsSubs = SkuDetailsParams.newBuilder()
          .setSkusList(listOf(MONTHLY_SUBSCRIPTION, YEARLY_SUBSCRIPTION))
          .setType(SkuType.SUBS)
          .build()

        val subsDetails = billingClient.querySkuDetails(paramsSubs)
        val subsItems = subsDetails.skuDetailsList ?: emptyList()
        uiState = PremiumUiModel(purchaseItems = subsItems, isLoading = false)
      } catch (error: Throwable) {
        Timber.e(error)
        uiState = PremiumUiModel(purchaseItems = emptyList(), isLoading = false)
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }
}
