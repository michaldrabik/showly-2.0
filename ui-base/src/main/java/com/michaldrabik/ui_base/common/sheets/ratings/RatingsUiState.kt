package com.michaldrabik.ui_base.common.sheets.ratings

import com.michaldrabik.ui_model.TraktRating

data class RatingsUiState(
  val isLoading: Boolean? = null,
  val rating: TraktRating? = null,
)
