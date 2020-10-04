package com.michaldrabik.ui_search

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_search.recycler.SearchListItem

data class SearchUiModel(
  val searchItems: List<SearchListItem>? = null,
  val searchItemsAnimate: Boolean? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val isSearching: Boolean? = null,
  val isEmpty: Boolean? = null,
  val isInitial: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SearchUiModel).copy(
      searchItems = newModel.searchItems ?: searchItems,
      searchItemsAnimate = newModel.searchItemsAnimate ?: searchItemsAnimate,
      recentSearchItems = newModel.recentSearchItems ?: recentSearchItems,
      isSearching = newModel.isSearching ?: isSearching,
      isEmpty = newModel.isEmpty ?: isEmpty,
      isInitial = newModel.isInitial ?: isInitial
    )

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
