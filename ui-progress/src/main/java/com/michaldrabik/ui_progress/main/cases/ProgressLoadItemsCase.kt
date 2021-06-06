package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress.ProgressItem
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.Locale.ROOT
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_local.database.model.Season as SeasonDb

@Singleton
class ProgressLoadItemsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadMyShows() = showsRepository.myShows.loadAll()

  suspend fun loadProgressItem(show: Show): ProgressItem {
    val episodes = database.episodesDao().getAllByShowId(show.traktId)
      .filter { it.seasonNumber != 0 }
    val seasons = database.seasonsDao().getAllForShow(show.traktId)
      .filter { it.seasonNumber != 0 }

    val episodesCount = episodes.count()
    val unwatchedEpisodes = episodes.filter { !it.isWatched }

    val nextEpisode = unwatchedEpisodes
      .filter { it.firstAired != null }
      .sortedWith(compareBy<EpisodeDb> { it.seasonNumber }.thenBy { it.episodeNumber })
      .firstOrNull()

    val upcomingEpisode = unwatchedEpisodes
      .filter { it.firstAired != null }
      .sortedBy { it.firstAired }
      .firstOrNull {
        val now = nowUtc().toLocalZone()
        val airtime = it.firstAired!!.toLocalZone()
        airtime.truncatedTo(DAYS) >= now.truncatedTo(DAYS)
      }

    val episodeUi = nextEpisode?.let { mappers.episode.fromDatabase(it) } ?: Episode.EMPTY
    val upcomingEpisodeUi = upcomingEpisode?.let { mappers.episode.fromDatabase(it) } ?: Episode.EMPTY

    val seasonUi = nextEpisode?.let { findSeason(seasons, it, episodes) } ?: Season.EMPTY
    val upcomingSeasonUi = upcomingEpisode?.let { findSeason(seasons, it, episodes) } ?: Season.EMPTY

    var showTranslation: Translation? = null
    var episodeTranslation: Translation? = null
    var upcomingTranslation: Translation? = null

    val language = translationsRepository.getLanguage()
    if (language != Config.DEFAULT_LANGUAGE) {
      showTranslation = translationsRepository.loadTranslation(show, language, true)
      episodeTranslation = translationsRepository.loadTranslation(episodeUi, show.ids.trakt, language, true)
      upcomingTranslation = translationsRepository.loadTranslation(upcomingEpisodeUi, show.ids.trakt, language, true)
    }

    return ProgressItem(
      show,
      seasonUi,
      upcomingSeasonUi,
      episodeUi,
      upcomingEpisodeUi,
      Image.createUnavailable(ImageType.POSTER),
      episodesCount,
      episodesCount - unwatchedEpisodes.count(),
      isPinned = pinnedItemsRepository.isItemPinned(show),
      showTranslation = showTranslation,
      episodeTranslation = episodeTranslation,
      upcomingEpisodeTranslation = upcomingTranslation
    )
  }

  private fun findSeason(
    seasons: List<SeasonDb>,
    episode: EpisodeDb,
    episodes: List<EpisodeDb>,
  ) = seasons
    .firstOrNull { it.seasonNumber == episode.seasonNumber }
    ?.let { season ->
      val seasonEpisodes = episodes.filter { e -> e.seasonNumber == season.seasonNumber }
      mappers.season.fromDatabase(season, seasonEpisodes)
    }

  fun prepareItems(
    input: List<ProgressItem>,
    searchQuery: String,
    sortOrder: SortOrder,
    upcomingEnabled: Boolean = true,
  ): List<ProgressItem> {

    val items = input
      .filter { it.episodesCount != 0 && it.episode.firstAired != null }
      .groupBy { it.episode.hasAired(it.season) }

    val aired = (items[true] ?: emptyList())
      .sortedWith(
        when (sortOrder) {
          NAME -> compareBy {
            val translatedTitle = if (it.showTranslation?.hasTitle == false) null else it.showTranslation?.title
            (translatedTitle ?: it.show.titleNoThe).toUpperCase(ROOT)
          }
          RECENTLY_WATCHED -> compareByDescending { it.show.updatedAt }
          NEWEST -> compareByDescending { it.episode.firstAired?.toMillis() }
          EPISODES_LEFT -> compareBy { it.episodesCount - it.watchedEpisodesCount }
          else -> throw IllegalStateException("Invalid sort order")
        }
      )

    val notAiredItems =
      if (!upcomingEnabled) emptyList()
      else items[false] ?: emptyList()

    val notAired = notAiredItems
      .sortedBy { it.episode.firstAired?.toInstant()?.toEpochMilli() }

    return (aired + notAired)
      .filter {
        if (searchQuery.isBlank()) true
        else it.show.title.contains(searchQuery, true) ||
          it.episode.title.contains(searchQuery, true) ||
          it.showTranslation?.title?.contains(searchQuery, true) == true ||
          it.episodeTranslation?.title?.contains(searchQuery, true) == true
      }
  }

  fun loadDateFormat() = dateFormatProvider.loadFullHourFormat()
}
