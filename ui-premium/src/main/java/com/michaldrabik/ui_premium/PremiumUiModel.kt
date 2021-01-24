package com.michaldrabik.ui_premium

import com.android.billingclient.api.SkuDetails
import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent

data class PremiumUiModel(
  val isLoading: Boolean? = null,
  val purchaseItems: List<SkuDetails>? = null,
  val finishEvent: ActionEvent<Boolean>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as PremiumUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      purchaseItems = newModel.purchaseItems ?: purchaseItems,
      finishEvent = newModel.finishEvent ?: finishEvent
    )
}
