package com.michaldrabik.ui_progress

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import org.threeten.bp.format.DateTimeFormatter

data class ProgressItem(
  override val show: Show,
  val season: Season,
  val upcomingSeason: Season,
  val episode: Episode,
  val upcomingEpisode: Episode,
  override val image: Image,
  val episodesCount: Int,
  val watchedEpisodesCount: Int,
  override val isLoading: Boolean = false,
  val headerTextResId: Int? = null,
  val isPinned: Boolean = false,
  val showTranslation: Translation? = null,
  val episodeTranslation: Translation? = null,
  val upcomingEpisodeTranslation: Translation? = null,
  val dateFormat: DateTimeFormatter? = null
) : ListItem {

  fun isSameAs(other: ProgressItem) =
    show.ids.trakt == other.show.ids.trakt && isHeader() == other.isHeader()

  fun isHeader() = headerTextResId != null

  fun isNew() = episode.firstAired?.isBefore(nowUtc()) ?: false &&
    nowUtcMillis() - (episode.firstAired?.toMillis() ?: 0) < Config.NEW_BADGE_DURATION

  companion object {
    val EMPTY = ProgressItem(
      Show.EMPTY,
      Season.EMPTY,
      Season.EMPTY,
      Episode.EMPTY,
      Episode.EMPTY,
      Image.createUnavailable(ImageType.POSTER),
      0,
      0
    )
  }
}
