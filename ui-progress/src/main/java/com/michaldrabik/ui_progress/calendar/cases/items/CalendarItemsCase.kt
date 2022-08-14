package com.michaldrabik.ui_progress.calendar.cases.items

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress.calendar.helpers.WatchlistAppender
import com.michaldrabik.ui_progress.calendar.helpers.filters.CalendarFilter
import com.michaldrabik.ui_progress.calendar.helpers.groupers.CalendarGrouper
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
abstract class CalendarItemsCase constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val watchlistAppender: WatchlistAppender,
) {

  abstract val filter: CalendarFilter
  abstract val grouper: CalendarGrouper

  abstract fun sortEpisodes(): Comparator<Episode>
  abstract fun isWatched(episode: Episode): Boolean

  suspend fun loadItems(searchQuery: String? = "") =
    withContext(Dispatchers.Default) {
      val now = nowUtc().toLocalZone()

      val language = translationsRepository.getLanguage()
      val dateFormat = dateFormatProvider.loadFullHourFormat()

      val (myShows, watchlistShows) = coroutineScope {
        val async1 = async { showsRepository.myShows.loadAll() }
        val async2 = async { showsRepository.watchlistShows.loadAll() }
        awaitAll(async1, async2)
      }

      val shows = myShows + watchlistShows

      val showsIds = shows.map { it.traktId }.chunked(250)
      val watchlistShowsIds = watchlistShows.map { it.traktId }

      val (episodes, seasons) = awaitAll(
        async {
          showsIds.fold(mutableListOf<Episode>()) { acc, list ->
            acc += localSource.episodes.getAllByShowsIds(list)
            acc
          }
        },
        async {
          showsIds.fold(mutableListOf<Season>()) { acc, list ->
            acc += localSource.seasons.getAllByShowsIds(list)
            acc
          }
        }
      )

      val filteredSeasons = (seasons as List<Season>).filter { it.seasonNumber != 0 }.toMutableList()
      val filteredEpisodes = (episodes as List<Episode>).filter { it.seasonNumber != 0 }.toMutableList()

      watchlistAppender.appendWatchlistShows(
        watchlistShows,
        filteredSeasons,
        filteredEpisodes
      )

      val elements = filteredEpisodes
        .filter { filter.filter(now, it) }
        .sortedWith(sortEpisodes())
        .map { episode ->
          async {
            val show = shows.firstOrNull { it.traktId == episode.idShowTrakt }
            val season = filteredSeasons.firstOrNull { it.idShowTrakt == episode.idShowTrakt && it.seasonNumber == episode.seasonNumber }

            if (show == null || season == null) {
              return@async null
            }

            val seasonEpisodes = episodes.filter { it.idShowTrakt == season.idShowTrakt && it.seasonNumber == season.seasonNumber }

            val episodeUi = mappers.episode.fromDatabase(episode)
            val seasonUi = mappers.season.fromDatabase(season, seasonEpisodes)

            var translations: TranslationsBundle? = null
            if (language != Config.DEFAULT_LANGUAGE) {
              translations = TranslationsBundle(
                episode = translationsRepository.loadTranslation(episodeUi, show.ids.trakt, language, onlyLocal = true),
                show = translationsRepository.loadTranslation(show, language, onlyLocal = true)
              )
            }
            CalendarListItem.Episode(
              show = show,
              image = imagesProvider.findCachedImage(show, ImageType.POSTER),
              episode = episodeUi,
              season = seasonUi,
              isWatched = isWatched(episode),
              isWatchlist = show.traktId in watchlistShowsIds,
              dateFormat = dateFormat,
              translations = translations
            )
          }
        }
        .awaitAll()
        .filterNotNull()

      val queryElements = filterByQuery(searchQuery ?: "", elements)
      grouper.groupByTime(queryElements)
    }

  private fun filterByQuery(query: String, items: List<CalendarListItem.Episode>) =
    items.filter {
      it.show.title.contains(query, true) ||
        it.episode.title.contains(query, true) ||
        it.translations?.show?.title?.contains(query, true) == true ||
        it.translations?.episode?.title?.contains(query, true) == true
    }
}
