package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ProgressType
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.helpers.ProgressItemsSorter
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.ui_model.Episode.Companion as EpisodeUi

@Suppress("UNCHECKED_CAST")
@Singleton
class ProgressItemsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val sorter: ProgressItemsSorter,
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

  suspend fun loadItems(searchQuery: String): List<ProgressListItem> = withContext(Dispatchers.Default) {
    val settings = settingsRepository.load()
    val upcomingEnabled = settings.progressUpcomingEnabled
    val sortOrder = settings.progressSortOrder
    val progressType = settingsRepository.progressPercentType
    val language = translationsRepository.getLanguage()
    val dateFormat = dateFormatProvider.loadFullHourFormat()

    val nowUtc = nowUtc()
    val upcomingLimit = nowUtc.plusMonths(UPCOMING_MONTHS_LIMIT).toMillis()
    val shows = showsRepository.myShows.loadAll()

    val items = shows.map { show ->
      async {
        val nextEpisode = database.episodesDao().getFirstUnwatched(show.traktId, upcomingLimit)
        val isUpcoming = nextEpisode?.firstAired?.isAfter(nowUtc) == true

        val episodeUi = nextEpisode?.let { mappers.episode.fromDatabase(it) }
        val seasonUi = nextEpisode?.let { ep ->
          database.seasonsDao().getById(ep.idSeason)?.let {
            mappers.season.fromDatabase(it)
          }
        }

        ProgressListItem.Episode(
          show = show,
          image = Image.createUnavailable(ImageType.POSTER),
          episode = episodeUi,
          season = seasonUi,
          totalCount = 0,
          watchedCount = 0,
          isUpcoming = isUpcoming,
          isPinned = false,
          dateFormat = dateFormat
        )
      }
    }.awaitAll()

    val validItems = items
      .filter { if (upcomingEnabled) true else !it.isUpcoming }
      .filter { it.episode?.firstAired != null }

    val filledItems = validItems
      .map {
        async {
          val image = imagesProvider.findCachedImage(it.show, ImageType.POSTER)
          val isPinned = pinnedItemsRepository.isItemPinned(it.show)

          var translations: TranslationsBundle? = null
          if (language != Config.DEFAULT_LANGUAGE) {
            translations = TranslationsBundle(
              show = translationsRepository.loadTranslation(it.show, language, onlyLocal = true),
              episode = translationsRepository.loadTranslation(it.episode ?: EpisodeUi.EMPTY, it.show.ids.trakt, language, onlyLocal = true)
            )
          }

          val (total, watched) = when (progressType) {
            ProgressType.AIRED -> {
              awaitAll(
                async { database.episodesDao().getTotalCount(it.show.traktId, nowUtc.toMillis()) },
                async { database.episodesDao().getWatchedCount(it.show.traktId, nowUtc.toMillis()) }
              )
            }
            ProgressType.ALL -> {
              awaitAll(
                async { database.episodesDao().getTotalCount(it.show.traktId) },
                async { database.episodesDao().getWatchedCount(it.show.traktId) }
              )
            }
          }

          it.copy(
            image = image,
            isPinned = isPinned,
            translations = translations,
            watchedCount = watched,
            totalCount = total
          )
        }
      }.awaitAll()

    val filteredItems = filterByQuery(searchQuery, filledItems)
    groupItems(filteredItems, sortOrder)
  }

  private fun filterByQuery(query: String, items: List<ProgressListItem.Episode>) =
    items.filter {
      it.show.title.contains(query, true) ||
        it.episode?.title?.contains(query, true) == true ||
        it.translations?.show?.title?.contains(query, true) == true ||
        it.translations?.episode?.title?.contains(query, true) == true
    }

  private fun groupItems(
    input: List<ProgressListItem.Episode>,
    sortOrder: SortOrder,
  ): List<ProgressListItem> {
    val pinnedItems = input
      .filter { it.isPinned }
      .sortedWith(sorter.sort(sortOrder))

    val groupedItems = (input - pinnedItems)
      .groupBy { !it.isUpcoming }

    val aired = ((groupedItems[true] ?: emptyList()))
      .sortedWith(sorter.sort(sortOrder))

    val upcoming = ((groupedItems[false] ?: emptyList()))
      .sortedBy { it.episode?.firstAired?.toMillis() }

    return when {
      upcoming.isEmpty() -> (pinnedItems + aired)
      else -> {
        val upcomingHeader = ProgressListItem.Header.create(R.string.textWatchlistIncoming)
        (pinnedItems + aired + upcomingHeader + upcoming)
      }
    }
  }
}
