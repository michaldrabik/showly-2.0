package com.michaldrabik.ui_lists.details.recycler

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import org.threeten.bp.ZonedDateTime

data class ListDetailsItem(
  val rank: Long,
  val show: Show?,
  val movie: Movie?,
  val image: Image,
  val translation: Translation?,
  val isLoading: Boolean,
  val listedAt: ZonedDateTime
) {

  fun getId(): String {
    val type = if (show != null) "show" else "movie"
    return "$type${show?.traktId ?: movie?.traktId}"
  }

  fun getTitleNoThe(): String {
    if (isShow()) return requireShow().titleNoThe
    if (isMovie()) return requireMovie().titleNoThe
    throw IllegalStateException()
  }

  fun getYear(): Int {
    if (isShow()) return requireShow().year
    if (isMovie()) return requireMovie().year
    throw IllegalStateException()
  }

  fun getRating(): Float {
    if (isShow()) return requireShow().rating
    if (isMovie()) return requireMovie().rating
    throw IllegalStateException()
  }

  fun isShow() = show != null

  fun isMovie() = movie != null

  fun requireShow() = show!!

  fun requireMovie() = movie!!
}
