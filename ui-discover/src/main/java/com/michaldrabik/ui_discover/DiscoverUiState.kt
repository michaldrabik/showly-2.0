package com.michaldrabik.ui_discover

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverUiState(
  val items: List<DiscoverListItem>? = null,
  val isLoading: Boolean? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: ActionEvent<Boolean>? = null,
)
