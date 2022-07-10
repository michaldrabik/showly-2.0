package com.michaldrabik.ui_search.helpers

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_search.recycler.SearchListItem
import java.util.UUID

object TestData {

  val SEARCH_LIST_ITEM = SearchListItem(
    id = UUID.randomUUID(),
    show = Show.EMPTY,
    movie = Movie.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    translation = null,
    score = 0F,
    isFollowed = false,
    isLoading = false,
    isWatchlist = false,
  )
}
