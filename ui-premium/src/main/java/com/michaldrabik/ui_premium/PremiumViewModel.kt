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
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_repository.SettingsRepository
import kotlinx.coroutines.delay
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
  }

  fun loadBilling(billingClient: BillingClient) {
    uiState = PremiumUiModel(isLoading = true)
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.d("BillingClient Setup Finished ${billingResult.debugMessage}")
        if (billingResult.responseCode == BillingResponseCode.OK) {
          checkOwnedPurchases(billingClient)
        }
      }

      override fun onBillingServiceDisconnected() {
        Timber.e("BillingClient Disconnected")
        //TODO
      }
    })
  }

  private fun checkOwnedPurchases(billingClient: BillingClient) {
    Timber.d("checkOwnedPurchases")
    viewModelScope.launch {
      uiState = PremiumUiModel(isLoading = true)

      try {
        val subscriptions = billingClient.queryPurchases(SkuType.SUBS)
        val purchases = subscriptions.purchasesList ?: emptyList()
        if (purchases.any {
            val json = JSONObject(it.originalJson)
            val productId = json.optString("productId", "")
            it.isAcknowledged && productId in arrayOf(MONTHLY_SUBSCRIPTION, YEARLY_SUBSCRIPTION)
          }) {
          settingsRepository.isPremium = true
          delay(250)
          _messageLiveData.value = MessageEvent.info(R.string.textPurchaseThanks)
          uiState = PremiumUiModel(finishEvent = ActionEvent(true))
        } else {
          loadPurchases(billingClient)
        }
        Timber.d("checkOwnedPurchases")
      } catch (error: Throwable) {
        Timber.e(error)
        uiState = PremiumUiModel(purchaseItems = emptyList(), isLoading = false)
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
      }
    }
  }
}
