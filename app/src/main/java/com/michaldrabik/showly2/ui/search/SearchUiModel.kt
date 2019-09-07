package com.michaldrabik.showly2.ui.search

import com.michaldrabik.showly2.ui.search.recycler.SearchListItem

data class SearchUiModel(
  val searchItems: List<SearchListItem>? = null,
  val isSearching: Boolean? = null,
  val updateListItem: SearchListItem? = null
)