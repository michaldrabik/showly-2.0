package com.michaldrabik.showly2.ui.search

import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem

data class SearchUiModel(
  val searchItems: List<SearchListItem>? = null,
  val searchItemsAnimate: Boolean? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val isSearching: Boolean? = null,
  val isEmpty: Boolean? = null,
  val isInitial: Boolean? = null,
  val error: Error? = null
) : UiModel {

  companion object {
    fun createLoading() = SearchUiModel(
      emptyList(),
      false,
      emptyList(),
      isSearching = true,
      isEmpty = false,
      isInitial = false
    )

    fun createResults(items: List<SearchListItem>) = SearchUiModel(
      items,
      searchItemsAnimate = true,
      isSearching = false,
      isEmpty = items.isEmpty(),
      isInitial = false
    )
  }
}