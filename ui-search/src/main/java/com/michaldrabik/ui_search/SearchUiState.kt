package com.michaldrabik.ui_search

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_search.recycler.SearchListItem

data class SearchUiState(
  val searchItems: List<SearchListItem>? = null,
  val searchItemsAnimate: ActionEvent<Boolean>? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val suggestionsItems: List<SearchListItem>? = null,
  val isSearching: Boolean = false,
  val isEmpty: Boolean = false,
  val isInitial: Boolean = false,
)
