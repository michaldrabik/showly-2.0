package com.michaldrabik.ui_discover

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverUiModel(
  val shows: List<DiscoverListItem>? = null,
  val showLoading: Boolean? = null,
  var filters: DiscoverFilters? = null,
  var scrollToTop: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as DiscoverUiModel).copy(
      shows = newModel.shows ?: shows,
      showLoading = newModel.showLoading ?: showLoading,
      filters = newModel.filters ?: filters,
      scrollToTop = newModel.scrollToTop ?: scrollToTop
    )
}
