package com.michaldrabik.ui_lists.details.recycler

import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.Translation
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class ListDetailsItem(
  val id: Long,
  val rank: Long,
  val rankDisplay: Int,
  val show: Show?,
  val movie: Movie?,
  val image: Image,
  val translation: Translation?,
  val userRating: Int?,
  val isLoading: Boolean,
  val isRankDisplayed: Boolean,
  val isManageMode: Boolean,
  val isEnabled: Boolean,
  val isWatched: Boolean,
  val isWatchlist: Boolean,
  val listedAt: ZonedDateTime,
  val sortOrder: SortOrder
) {

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

  fun getDate(): Long {
    if (isShow()) {
      return if (requireShow().firstAired.isBlank()) 0 else ZonedDateTime.parse(requireShow().firstAired).toMillis()
    }
    if (isMovie()) {
      return requireMovie().released?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: 0
    }
    throw IllegalStateException()
  }

  fun getRating(): Float {
    if (isShow()) return requireShow().rating
    if (isMovie()) return requireMovie().rating
    throw IllegalStateException()
  }

  fun getTraktId(): IdTrakt {
    if (isShow()) return IdTrakt(requireShow().traktId)
    if (isMovie()) return IdTrakt(requireMovie().traktId)
    throw IllegalStateException()
  }

  fun isShow() = show != null

  fun isMovie() = movie != null

  fun requireShow() = show!!

  fun requireMovie() = movie!!
}
