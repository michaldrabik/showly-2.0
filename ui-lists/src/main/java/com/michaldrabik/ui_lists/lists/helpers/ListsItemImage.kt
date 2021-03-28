package com.michaldrabik.ui_lists.lists.helpers

import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show

data class ListsItemImage(
  val image: Image,
  val show: Show? = null,
  val movie: Movie? = null
) {

  fun getIds(): Ids? {
    if (show != null) return show.ids
    if (movie != null) return movie.ids
    return null
  }

  fun isShow() = show != null

  fun isMovie() = movie != null
}
