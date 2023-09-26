package com.michaldrabik.ui_premium

import com.android.billingclient.api.ProductDetails
import com.michaldrabik.ui_base.utilities.events.Event

data class PremiumUiState(
  val isLoading: Boolean = false,
  val isPurchasePending: Boolean = false,
  val purchaseItems: List<ProductDetails>? = null,
  val onFinish: Event<Boolean>? = null,
)
