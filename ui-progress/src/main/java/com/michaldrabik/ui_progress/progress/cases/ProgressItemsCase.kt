package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.repository.OnHoldItemsRepository
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.ProgressNextEpisodeType.LAST_WATCHED
import com.michaldrabik.ui_model.ProgressNextEpisodeType.OLDEST
import com.michaldrabik.ui_model.ProgressType
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.helpers.ProgressItemsSorter
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem.Header.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.ui_model.Episode.Companion as EpisodeUi

@Singleton
class ProgressItemsCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val ratingsRepository: RatingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val sorter: ProgressItemsSorter,
) {

  companion object {
    private const val UPCOMING_MONTHS_LIMIT = 3L
  }

  suspend fun loadItems(searchQuery: String): List<ProgressListItem> = withContext(Dispatchers.Default) {
    val settings = settingsRepository.load()
    val upcomingEnabled = settings.progressUpcomingEnabled
    val progressType = settingsRepository.progressPercentType
    val language = translationsRepository.getLanguage()
    val dateFormat = dateFormatProvider.loadFullHourFormat()

    val sortOrder = settingsRepository.sorting.progressShowsSortOrder
    val sortType = settingsRepository.sorting.progressShowsSortType
    val newAtTop = settingsRepository.sorting.progressShowsNewAtTop

    val nowUtc = nowUtc()
    val upcomingLimit = nowUtc.plusMonths(UPCOMING_MONTHS_LIMIT).toMillis()
    val shows = showsRepository.myShows.loadAll()
    val nextEpisodeType = settingsRepository.progressNextEpisodeType

    val items = shows.map { show ->
      async {
        val nextEpisode = findNextEpisode(show.traktId, nextEpisodeType, upcomingLimit)

        val episodeUi = nextEpisode?.let { mappers.episode.fromDatabase(it) }
        val seasonUi = nextEpisode?.let { ep ->
          localSource.seasons.getById(ep.idSeason)?.let {
            mappers.season.fromDatabase(it)
          }
        }
        val isUpcoming = nextEpisode?.firstAired?.isAfter(nowUtc) == true

        ProgressListItem.Episode(
          show = show,
          image = Image.createUnavailable(ImageType.POSTER),
          episode = episodeUi,
          season = seasonUi,
          totalCount = 0,
          watchedCount = 0,
          isUpcoming = isUpcoming,
          isPinned = false,
          isOnHold = false,
          dateFormat = dateFormat,
          sortOrder = sortOrder
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
          val rating = ratingsRepository.shows.loadRatings(listOf(it.show))
          val isPinned = pinnedItemsRepository.isItemPinned(it.show)
          val isOnHold = onHoldItemsRepository.isOnHold(it.show)

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
                async { localSource.episodes.getTotalCount(it.show.traktId, nowUtc.toMillis()) },
                async { localSource.episodes.getWatchedCount(it.show.traktId, nowUtc.toMillis()) }
              )
            }

            ProgressType.ALL -> {
              awaitAll(
                async { localSource.episodes.getTotalCount(it.show.traktId) },
                async { localSource.episodes.getWatchedCount(it.show.traktId) }
              )
            }
          }

          it.copy(
            image = image,
            isPinned = isPinned,
            isOnHold = isOnHold,
            translations = translations,
            userRating = rating.firstOrNull()?.rating,
            watchedCount = watched,
            totalCount = total
          )
        }
      }.awaitAll()

    val filteredItems = filterByQuery(searchQuery, filledItems)
    val groupedItems = groupItems(filteredItems, sortOrder, sortType, newAtTop)

    if (groupedItems.isNotEmpty()) {
      val filtersItem = loadFiltersItem(sortOrder, sortType)
      listOf(filtersItem) + groupedItems
    } else {
      groupedItems
    }
  }

  private suspend fun findNextEpisode(
    showId: Long,
    nextEpisodeType: ProgressNextEpisodeType,
    upcomingLimit: Long,
  ): Episode? = when (nextEpisodeType) {
    LAST_WATCHED -> {
      when (val lastWatchedEpisode = localSource.episodes.getLastWatched(showId)) {
        null -> localSource.episodes.getFirstUnwatched(showId, upcomingLimit)
        else -> localSource.episodes.getFirstUnwatchedAfterEpisode(
          showId,
          lastWatchedEpisode.seasonNumber,
          lastWatchedEpisode.episodeNumber,
          upcomingLimit
        )
      }
    }
    OLDEST -> {
      localSource.episodes.getFirstUnwatched(showId, upcomingLimit)
    }
  }

  private fun filterByQuery(query: String, items: List<ProgressListItem.Episode>) =
    items.filter {
      it.show.title.contains(query, true) ||
        it.episode?.title?.contains(query, true) == true ||
        it.translations?.show?.title?.contains(query, true) == true ||
        it.translations?.episode?.title?.contains(query, true) == true
    }

  private suspend fun groupItems(
    input: List<ProgressListItem.Episode>,
    sortOrder: SortOrder,
    sortType: SortType,
    newAtTop: Boolean,
  ): List<ProgressListItem> = coroutineScope {
    val (newItems, pinnedItems, onHoldItems) = awaitAll(
      async {
        if (newAtTop) {
          input
            .filter { it.isNew() && !it.isOnHold && !it.isPinned }
            .sortedWith(sorter.sort(sortOrder, sortType))
        } else {
          emptyList()
        }
      },
      async {
        input
          .filter { it.isPinned }
          .sortedWith(compareByDescending<ProgressListItem.Episode> { it.isNew() } then sorter.sort(sortOrder, sortType))
      },
      async {
        input
          .filter { it.isOnHold }
          .sortedWith(compareByDescending<ProgressListItem.Episode> { it.isNew() } then sorter.sort(sortOrder, sortType))
      }
    )

    val groupedItems = (input - newItems.toSet() - pinnedItems.toSet() - onHoldItems.toSet())
      .groupBy { !it.isUpcoming }

    val (airedItems, upcomingItems) = awaitAll(
      async {
        ((groupedItems[true] ?: emptyList()))
          .sortedWith(sorter.sort(sortOrder, sortType))
      },
      async {
        ((groupedItems[false] ?: emptyList()))
          .sortedBy { it.episode?.firstAired?.toMillis() }
      }
    )

    mutableListOf<ProgressListItem>().apply {
      if (pinnedItems.isNotEmpty()) {
        addAll(pinnedItems)
      }
      if (newItems.isNotEmpty()) {
        addAll(newItems)
      }
      if (airedItems.isNotEmpty()) {
        addAll(airedItems)
      }
      if (upcomingItems.isNotEmpty()) {
        val isCollapsed = settingsRepository.isProgressUpcomingCollapsed
        val upcomingHeader = ProgressListItem.Header.create(Type.UPCOMING, R.string.textWatchlistIncoming, isCollapsed)
        addAll(listOf(upcomingHeader))
        if (!isCollapsed) addAll(upcomingItems)
      }
      if (onHoldItems.isNotEmpty()) {
        val isCollapsed = settingsRepository.isProgressOnHoldCollapsed
        val onHoldHeader = ProgressListItem.Header.create(Type.ON_HOLD, R.string.textOnHold, isCollapsed)
        addAll(listOf(onHoldHeader))
        if (!isCollapsed) addAll(onHoldItems)
      }
    }
  }

  private fun loadFiltersItem(
    sortOrder: SortOrder,
    sortType: SortType,
  ): ProgressListItem.Filters {
    return ProgressListItem.Filters(
      sortOrder = sortOrder,
      sortType = sortType
    )
  }
}
