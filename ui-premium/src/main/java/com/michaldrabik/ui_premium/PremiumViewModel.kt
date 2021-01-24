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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PremiumViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : BaseViewModel<PremiumUiModel>() {

  fun loadBilling(billingClient: BillingClient) {
    viewModelScope.launch {
      uiState = PremiumUiModel(isLoading = true)
      billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
          Timber.d("BillingClient Setup Finished ${billingResult.debugMessage}")
          if (billingResult.responseCode == BillingResponseCode.OK) {
            loadPurchases(billingClient)
          }
        }

        override fun onBillingServiceDisconnected() {
          Timber.e("BillingClient Disconnected")
          //TODO
        }
      })
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
        BillingResponseCode.USER_CANCELED -> Unit
        else -> {
          //TODO
        }
      }
      uiState = PremiumUiModel(isLoading = false)
    }
  }

  private fun loadPurchases(billingClient: BillingClient) {
    viewModelScope.launch {
      uiState = PremiumUiModel(isLoading = true)

      val paramsInApp = SkuDetailsParams.newBuilder()
        .setSkusList(listOf("showly_premium_lifetime"))
        .setType(SkuType.INAPP)
        .build()

      val paramsSubs = SkuDetailsParams.newBuilder()
        .setSkusList(listOf("showly_premium_3_months"))
        .setType(SkuType.SUBS)
        .build()

      try {
        coroutineScope {
          val inAppsDetails = async { billingClient.querySkuDetails(paramsInApp) }
          val subsDetails = async { billingClient.querySkuDetails(paramsSubs) }
          val (inApps, subscriptions) = awaitAll(inAppsDetails, subsDetails)

          Timber.d("InApp Purchases: ${inApps.skuDetailsList?.size}")
          Timber.d("Subscriptions Purchases: ${subscriptions.skuDetailsList?.size}")

          val inAppItems = inApps.skuDetailsList ?: emptyList()
          val subsItems = subscriptions.skuDetailsList ?: emptyList()

          uiState = PremiumUiModel(purchaseItems = subsItems + inAppItems, isLoading = false)
        }
      } catch (error: Throwable) {
        Timber.e(error)
        uiState = PremiumUiModel(purchaseItems = emptyList(), isLoading = false)
      }
    }
  }
}
