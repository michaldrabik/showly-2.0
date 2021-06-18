package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ProgressType.AIRED
import com.michaldrabik.ui_model.ProgressType.ALL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.ui_model.Episode.Companion as EpisodeUi

@Suppress("UNCHECKED_CAST")
@Singleton
class ProgressItemsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  companion object {
    private const val UPCOMING_MONTHS_LIMIT = 3L
  }

  suspend fun loadItems(searchQuery: String): List<ProgressListItem> = withContext(Dispatchers.IO) {
    val settings = settingsRepository.load()
    val upcomingEnabled = settings.progressUpcomingEnabled
    val sortOrder = settings.progressSortOrder
    val progressType = settingsRepository.progressPercentType
    val language = translationsRepository.getLanguage()
    val dateFormat = dateFormatProvider.loadFullHourFormat()

    val nowUtc = nowUtc()
    val shows = showsRepository.myShows.loadAll()

    val items = shows.map { show ->
      async {

        val (episodes, seasons) = awaitAll(
          async { database.episodesDao().getAllByShowId(show.traktId) },
          async { database.seasonsDao().getAllByShowId(show.traktId) }
        )

        val filteredEpisodes = (episodes as List<Episode>).filter { it.seasonNumber != 0 }
        val filteredSeasons = (seasons as List<Season>).filter { it.seasonNumber != 0 }

        val airedEpisodes = filteredEpisodes.filter {
          it.firstAired != null && nowUtc.toMillis() >= it.firstAired!!.toMillis()
        }
        val unwatchedEpisodes = filteredEpisodes.filter { !it.isWatched }
        val unwatchedAiredEpisodes = airedEpisodes.filter { !it.isWatched }

        val nextEpisode = unwatchedAiredEpisodes
          .filter { it.firstAired != null }
          .sortedWith(compareBy<Episode> { it.seasonNumber }.thenBy { it.episodeNumber })
          .firstOrNull()

        var upcomingEpisode: Episode? = null
        if (nextEpisode == null) {
          upcomingEpisode = unwatchedEpisodes
            .filter { it.firstAired != null }
            .sortedBy { it.firstAired }
            .firstOrNull {
              val now = nowUtc.toLocalZone().truncatedTo(ChronoUnit.DAYS)
              val limit = now.plusMonths(UPCOMING_MONTHS_LIMIT)
              val airtime = it.firstAired!!.toLocalZone().truncatedTo(ChronoUnit.DAYS)
              airtime.isBefore(limit) && airtime >= now
            }
        }

        val isUpcoming = upcomingEpisode != null
        val episodeUi = nextEpisode?.let { mappers.episode.fromDatabase(it) }
        val seasonUi = nextEpisode?.let { findSeason(filteredSeasons, it, filteredEpisodes) }
        val upcomingEpisodeUi = upcomingEpisode?.let { mappers.episode.fromDatabase(it) }
        val upcomingSeasonUi = upcomingEpisode?.let { findSeason(filteredSeasons, it, filteredEpisodes) }

        val totalCount = when (progressType) {
          AIRED -> airedEpisodes.count()
          ALL -> filteredEpisodes.count()
        }

        val watchedCount = when (progressType) {
          AIRED -> airedEpisodes.count() - unwatchedAiredEpisodes.count()
          ALL -> filteredEpisodes.count() - unwatchedEpisodes.count()
        }

        var translations: TranslationsBundle? = null
        if (language != Config.DEFAULT_LANGUAGE) {
          translations = TranslationsBundle(
            show = translationsRepository.loadTranslation(show, language, onlyLocal = true),
            episode = translationsRepository.loadTranslation(episodeUi ?: EpisodeUi.EMPTY, show.ids.trakt, language, onlyLocal = true),
            upcomingEpisode = translationsRepository.loadTranslation(upcomingEpisodeUi ?: EpisodeUi.EMPTY, show.ids.trakt, language, onlyLocal = true)
          )
        }

        val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
        val isPinned = pinnedItemsRepository.isItemPinned(show)

        ProgressListItem.Episode(
          show = show,
          image = image,
          episode = if (isUpcoming) upcomingEpisodeUi else episodeUi,
          season = if (isUpcoming) upcomingSeasonUi else seasonUi,
          totalCount = totalCount,
          watchedCount = watchedCount,
          isUpcoming = isUpcoming,
          isPinned = isPinned,
          translations = translations,
          dateFormat = dateFormat
        )
      }
    }.awaitAll()

    val validItems = items
      .filter { if (upcomingEnabled) true else !it.isUpcoming }
      .filter { it.totalCount != 0 && it.episode != null && it.episode.firstAired != null }
    val filteredItems = filterByQuery(searchQuery, validItems)
    prepareItems(filteredItems, sortOrder)
  }

  private fun filterByQuery(query: String, items: List<ProgressListItem.Episode>) =
    items.filter {
      it.show.title.contains(query, true) ||
        it.episode?.title?.contains(query, true) == true ||
        it.translations?.show?.title?.contains(query, true) == true ||
        it.translations?.episode?.title?.contains(query, true) == true
    }

  private fun prepareItems(
    input: List<ProgressListItem.Episode>,
    sortOrder: SortOrder,
  ): List<ProgressListItem> {
    val pinnedItems = input.filter { it.isPinned }
    val groupedItems = input.groupBy { !it.isUpcoming }

    val aired = ((groupedItems[true] ?: emptyList()) - pinnedItems)
      .sortedWith(
        when (sortOrder) {
          SortOrder.NAME -> compareBy {
            val translatedTitle =
              if (it.translations?.show?.hasTitle == false) null
              else it.translations?.show?.title
            (translatedTitle ?: it.show.titleNoThe).uppercase(Locale.ROOT)
          }
          SortOrder.RECENTLY_WATCHED -> compareByDescending { it.show.updatedAt }
          SortOrder.NEWEST -> compareByDescending { it.episode?.firstAired?.toMillis() }
          SortOrder.EPISODES_LEFT -> compareBy { it.totalCount - it.watchedCount }
          else -> throw IllegalStateException("Invalid sort order")
        }
      )

    val upcoming = ((groupedItems[false] ?: emptyList()) - pinnedItems)
      .sortedBy { it.episode?.firstAired?.toMillis() }

    return when {
      upcoming.isEmpty() -> (pinnedItems + aired)
      else -> {
        val upcomingHeader = ProgressListItem.Header.create(R.string.textWatchlistIncoming)
        (pinnedItems + aired + upcomingHeader + upcoming)
      }
    }
  }

  private fun findSeason(
    seasons: List<Season>,
    episode: Episode,
    episodes: List<Episode>,
  ) = seasons
    .firstOrNull { it.seasonNumber == episode.seasonNumber }
    ?.let { season ->
      val seasonEpisodes = episodes.filter { e -> e.seasonNumber == season.seasonNumber }
      mappers.season.fromDatabase(season, seasonEpisodes)
    }
}
