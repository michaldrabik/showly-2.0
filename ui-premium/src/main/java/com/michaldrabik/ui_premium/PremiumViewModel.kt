package com.michaldrabik.ui_premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PENDING
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResult
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
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_premium.PremiumUiEvent.HighlightItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val purchaseItemsState = MutableStateFlow<List<SkuDetails>?>(null)
  private val purchasePendingState = MutableStateFlow(false)
  private val loadingState = MutableStateFlow(false)
  private val finishEvent = MutableStateFlow<Event<Boolean>?>(null)

  private var connectionsCount = 0

  fun loadBilling(billingClient: BillingClient) {
    loadingState.value = true
    connectionsCount += 1
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.d("BillingClient Setup Finished ${billingResult.debugMessage}")

        if (billingResult.responseCode == BillingResponseCode.OK) {
          connectionsCount = 0
          val featureResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
          val subscriptionsAvailable = featureResult.responseCode == BillingResponseCode.OK
          checkOwnedPurchases(billingClient, subscriptionsAvailable)
          if (!subscriptionsAvailable) {
            Analytics.logUnsupportedSubscriptions()
          }
        } else {
          Analytics.logUnsupportedBilling()
          messageChannel.trySend(MessageEvent.Error(R.string.errorSubscriptionsNotAvailable))
        }
      }

      override fun onBillingServiceDisconnected() {
        if (connectionsCount > 3) {
          Timber.e("BillingClient Disconnected. All retries failed.")
          messageChannel.trySend(MessageEvent.Error(R.string.errorGeneral))
          connectionsCount = 0
        } else {
          Timber.w("BillingClient Disconnected. Retrying...")
          loadBilling(billingClient)
        }
      }
    })
  }

  private fun checkOwnedPurchases(
    billingClient: BillingClient,
    subscriptionsAvailable: Boolean
  ) {
    Timber.d("checkOwnedPurchases")
    viewModelScope.launch {
      loadingState.value = true

      try {
        val subscriptions =
          if (subscriptionsAvailable) billingClient.queryPurchasesAsync(SkuType.SUBS)
          else PurchasesResult(BillingResult(), emptyList())
        val inApps = billingClient.queryPurchasesAsync(SkuType.INAPP)
        val purchases = subscriptions.purchasesList + inApps.purchasesList

        val eligibleProducts = mutableListOf(PREMIUM_MONTHLY_SUBSCRIPTION, PREMIUM_YEARLY_SUBSCRIPTION, PREMIUM_LIFETIME_INAPP)
        if (Config.PROMOS_ENABLED) {
          eligibleProducts.add(PREMIUM_LIFETIME_INAPP_PROMO)
        }
        val eligiblePurchases = purchases.filter {
          val json = JSONObject(it.originalJson)
          val productId = json.optString("productId", "")
          productId in eligibleProducts
        }

        when {
          eligiblePurchases.any { it.isAcknowledged && it.purchaseState == PURCHASED } -> unlockAndFinish()
          eligiblePurchases.any { !it.isAcknowledged && it.purchaseState == PURCHASED } -> {
            val purchase = eligiblePurchases.first { !it.isAcknowledged && it.purchaseState == PURCHASED }
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
            billingClient.acknowledgePurchase(params.build())
            unlockAndFinish()
          }
          eligiblePurchases.any { it.purchaseState == PENDING } -> {
            purchasePendingState.value = true
            loadingState.value = false
          }
          else -> loadPurchases(billingClient, subscriptionsAvailable)
        }
      } catch (error: Throwable) {
        purchaseItemsState.value = emptyList()
        loadingState.value = false
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        Timber.e(error)
      }
    }
  }

  fun handlePurchase(
    billingClient: BillingClient,
    billingResult: BillingResult,
    purchases: MutableList<Purchase>,
  ) {
    viewModelScope.launch {
      loadingState.value = true
      Timber.d("${billingResult.responseCode} ${purchases.size}")

      when (billingResult.responseCode) {
        BillingResponseCode.OK -> {
          if (purchases.isNotEmpty()) {
            val purchase = purchases.first()
            if (purchase.purchaseState == PURCHASED && !purchase.isAcknowledged) {
              val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
              billingClient.acknowledgePurchase(params.build())
              unlockAndFinish()
            }
          }
        }
        BillingResponseCode.ITEM_ALREADY_OWNED -> unlockAndFinish()
        BillingResponseCode.USER_CANCELED -> loadingState.value = false
        else -> loadingState.value = false
      }
    }
  }

  private fun loadPurchases(
    billingClient: BillingClient,
    subscriptionsAvailable: Boolean
  ) {
    viewModelScope.launch {
      try {
        loadingState.value = true

        val inAppsEnabled = Firebase.remoteConfig.getBoolean("in_app_enabled")

        val paramsSubs = SkuDetailsParams.newBuilder()
          .setSkusList(listOf(PREMIUM_MONTHLY_SUBSCRIPTION, PREMIUM_YEARLY_SUBSCRIPTION))
          .setType(SkuType.SUBS)
          .build()

        val paramsInApps = SkuDetailsParams.newBuilder()
          .setSkusList(listOf(PREMIUM_LIFETIME_INAPP))
          .setType(SkuType.INAPP)
          .build()

        val subsDetails =
          if (subscriptionsAvailable) billingClient.querySkuDetails(paramsSubs)
          else SkuDetailsResult(BillingResult(), emptyList())
        val inAppsDetails = billingClient.querySkuDetails(paramsInApps)

        val subsItems = subsDetails.skuDetailsList ?: emptyList()
        val inAppsItems = if (inAppsEnabled) inAppsDetails.skuDetailsList ?: emptyList() else emptyList()

        purchaseItemsState.value = subsItems + inAppsItems
        loadingState.value = false
      } catch (error: Throwable) {
        Timber.e(error)
        purchaseItemsState.value = emptyList()
        loadingState.value = false
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
      }
    }
  }

  private suspend fun unlockAndFinish() {
    settingsRepository.isPremium = true
    messageChannel.send(MessageEvent.Info(R.string.textPurchaseThanks))
    finishEvent.value = Event(true)
  }

  fun highlightItem(item: com.michaldrabik.ui_model.PremiumFeature) {
    viewModelScope.launch {
      delay(300)
      eventChannel.send(HighlightItem(item))
    }
  }

  val uiState = combine(
    purchaseItemsState,
    purchasePendingState,
    loadingState,
    finishEvent
  ) { s1, s2, s3, s4 ->
    PremiumUiState(
      purchaseItems = s1,
      isPurchasePending = s2,
      isLoading = s3,
      onFinish = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = PremiumUiState()
  )
}
