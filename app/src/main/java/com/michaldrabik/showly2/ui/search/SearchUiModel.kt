package com.michaldrabik.showly2.ui.search

import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem

data class SearchUiModel(
  val searchItems: List<SearchListItem>? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val isSearching: Boolean? = null,
  val isEmpty: Boolean? = null,
  val updateListItem: SearchListItem? = null,
  val error: Error? = null
)