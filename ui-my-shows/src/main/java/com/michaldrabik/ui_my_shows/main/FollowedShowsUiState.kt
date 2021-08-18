package com.michaldrabik.ui_my_shows.main

import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsSearchResult

data class FollowedShowsUiState(
  val searchResult: MyShowsSearchResult? = null,
)
