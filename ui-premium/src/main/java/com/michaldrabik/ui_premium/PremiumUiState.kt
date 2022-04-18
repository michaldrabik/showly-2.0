package com.michaldrabik.ui_premium

import com.android.billingclient.api.SkuDetails
import com.michaldrabik.ui_base.utilities.events.Event

data class PremiumUiState(
  val isLoading: Boolean = false,
  val isPurchasePending: Boolean = false,
  val purchaseItems: List<SkuDetails>? = null,
  val onFinish: Event<Boolean>? = null,
)
