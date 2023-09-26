package com.michaldrabik.ui_premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.FeatureType
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
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

private const val INDIA_CURRENCY_CODE = "INR"

@HiltViewModel
class PremiumViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val purchaseItemsState = MutableStateFlow<List<ProductDetails>?>(null)
  private val purchasePendingState = MutableStateFlow(false)
  private val loadingState = MutableStateFlow(false)
  private val finishEvent = MutableStateFlow<Event<Boolean>?>(null)

  private var connectionsCount = 0

  fun loadBilling(billingClient: BillingClient) {
    if (billingClient.isReady) {
      return
    }

    loadingState.value = true
    connectionsCount += 1

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.d("BillingClient Setup Finished ${billingResult.debugMessage}")

        if (billingResult.responseCode == OK) {
          connectionsCount = 0

          val isSubscriptionSupported = billingClient
            .isFeatureSupported(FeatureType.SUBSCRIPTIONS).responseCode == OK

          val isInAppsSupported = billingClient
            .isFeatureSupported(FeatureType.PRODUCT_DETAILS).responseCode == OK

          if (!isSubscriptionSupported) Analytics.logUnsupportedSubscriptions()
          if (!isInAppsSupported) Analytics.logUnsupportedProductDetails()

          checkOwnedPurchases(billingClient, isSubscriptionSupported, isInAppsSupported)
        } else {
          Analytics.logUnsupportedBilling(billingResult.responseCode)
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
    subscriptionsSupported: Boolean,
    inAppsSupported: Boolean
  ) {
    Timber.d("checkOwnedPurchases")
    viewModelScope.launch {
      loadingState.value = true
      try {
        if (!inAppsSupported && !subscriptionsSupported) {
          messageChannel.trySend(MessageEvent.Error(R.string.errorBillingProductsNotAvailable))
          return@launch
        }

        val subscriptions =
          if (subscriptionsSupported) {
            billingClient.queryPurchasesAsync(
              QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.SUBS)
                .build()
            )
          } else {
            PurchasesResult(BillingResult(), emptyList())
          }

        val inApps =
          if (inAppsSupported) {
            billingClient.queryPurchasesAsync(
              QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.INAPP)
                .build()
            )
          } else {
            PurchasesResult(BillingResult(), emptyList())
          }

        val eligibleProducts = mutableListOf(
          PREMIUM_MONTHLY_SUBSCRIPTION,
          PREMIUM_YEARLY_SUBSCRIPTION,
          PREMIUM_LIFETIME_INAPP
        )
        if (Config.PROMOS_ENABLED) {
          eligibleProducts.add(PREMIUM_LIFETIME_INAPP_PROMO)
        }

        val eligiblePurchases = (subscriptions.purchasesList + inApps.purchasesList)
          .filter {
            val json = JSONObject(it.originalJson)
            val productId = json.optString("productId", "")
            productId in eligibleProducts
          }

        when {
//          eligiblePurchases.any { it.isAcknowledged && it.purchaseState == PURCHASED } -> unlockAndFinish()
//          eligiblePurchases.any { !it.isAcknowledged && it.purchaseState == PURCHASED } -> {
//            val purchase = eligiblePurchases.first { !it.isAcknowledged && it.purchaseState == PURCHASED }
//            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
//            billingClient.acknowledgePurchase(params.build())
//            unlockAndFinish()
//          }
//          eligiblePurchases.any { it.purchaseState == PENDING } -> {
//            purchasePendingState.value = true
//            loadingState.value = false
//          }
          else -> loadPurchases(billingClient, subscriptionsSupported)
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
        OK -> {
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

        val paramsSubs = QueryProductDetailsParams.newBuilder()
          .setProductList(
            listOf(
              QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_MONTHLY_SUBSCRIPTION)
                .setProductType(ProductType.SUBS)
                .build(),
              QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_YEARLY_SUBSCRIPTION)
                .setProductType(ProductType.SUBS)
                .build()
            )
          )
          .build()

        val paramsInApps = QueryProductDetailsParams.newBuilder()
          .setProductList(
            listOf(
              QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_LIFETIME_INAPP)
                .setProductType(ProductType.INAPP)
                .build()
            )
          )
          .build()

        val subsDetails = if (subscriptionsAvailable) {
          billingClient.queryProductDetails(paramsSubs)
        } else {
          ProductDetailsResult(BillingResult(), emptyList())
        }
        val inAppsDetails = billingClient.queryProductDetails(paramsInApps)

        val subsItems = (subsDetails.productDetailsList ?: emptyList())
          .filter {
            val priceCode = it.subscriptionOfferDetails
              ?.firstOrNull()
              ?.pricingPhases
              ?.pricingPhaseList
              ?.firstOrNull()
              ?.priceCurrencyCode
            priceCode != INDIA_CURRENCY_CODE
          }
        val inAppsItems = if (inAppsEnabled) {
          inAppsDetails.productDetailsList ?: emptyList()
        } else {
          emptyList()
        }

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
