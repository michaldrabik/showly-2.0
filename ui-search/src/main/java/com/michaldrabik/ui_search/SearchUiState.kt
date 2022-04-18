package com.michaldrabik.ui_search

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.utilities.SearchOptions

data class SearchUiState(
  val searchItems: List<SearchListItem>? = null,
  val searchItemsAnimate: Event<Boolean>? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val suggestionsItems: List<SearchListItem>? = null,
  val searchOptions: SearchOptions? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
  val isSearching: Boolean = false,
  val isEmpty: Boolean = false,
  val isInitial: Boolean = false,
  val isFiltersVisible: Boolean = false,
  val isMoviesEnabled: Boolean = false,
  val resetScroll: Event<Boolean>? = null,
)
