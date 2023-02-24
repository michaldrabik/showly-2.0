package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_lists.details.helpers.ListDetailsSorter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Collections
import javax.inject.Inject

@ViewModelScoped
class ListDetailsItemsCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val listsRepository: ListsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val sorter: ListDetailsSorter,
) {

  suspend fun loadItems(list: CustomList): Pair<List<ListDetailsItem>, Int> =
    coroutineScope {
      val moviesEnabled = settingsRepository.isMoviesEnabled
      val language = translationsRepository.getLanguage()
      val listItems = listsRepository.loadItemsById(list.id)

      val showsAsync = async {
        val ids = listItems.filter { it.type == SHOWS.type }.map { it.idTrakt }
        localSource.shows.getAllChunked(ids)
      }
      val moviesAsync = async {
        val ids = listItems.filter { it.type == MOVIES.type }.map { it.idTrakt }
        localSource.movies.getAllChunked(ids)
      }

      val showsTranslationsAsync = async {
        if (language == Config.DEFAULT_LANGUAGE) emptyMap()
        else translationsRepository.loadAllShowsLocal(language)
      }
      val moviesTranslationsAsync = async {
        if (language == Config.DEFAULT_LANGUAGE) emptyMap()
        else translationsRepository.loadAllMoviesLocal(language)
      }

      val showsRatingsAsync = async {
        ratingsRepository.shows.loadShowsRatings()
      }
      val moviesRatingsAsync = async {
        ratingsRepository.movies.loadMoviesRatings()
      }

      val (shows, movies) = Pair(showsAsync.await(), moviesAsync.await())
      val (showsTranslations, moviesTranslations) = Pair(showsTranslationsAsync.await(), moviesTranslationsAsync.await())
      val (showsRatings, moviesRatings) = Pair(showsRatingsAsync.await(), moviesRatingsAsync.await())

      val isRankSort = list.sortByLocal == SortOrder.RANK
      val itemsToDelete = Collections.synchronizedList(mutableListOf<CustomListItem>())
      val items = listItems.map { listItem ->
        async {
          val listedAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(listItem.listedAt), ZoneId.of("UTC"))
          when (listItem.type) {
            SHOWS.type -> {
              val listShow = shows.firstOrNull { it.idTrakt == listItem.idTrakt }
              if (listShow == null) {
                itemsToDelete.add(listItem)
                return@async null
              }
              val show = mappers.show.fromDatabase(listShow)
              val translation = showsTranslations[show.traktId]
              val rating = showsRatings.find { it.idTrakt == show.ids.trakt }
              createListDetailsItem(show, listItem, translation, rating, isRankSort, listedAt, list.sortByLocal)
            }
            MOVIES.type -> {
              val listMovie = movies.firstOrNull { it.idTrakt == listItem.idTrakt }
              if (listMovie == null) {
                itemsToDelete.add(listItem)
                return@async null
              }
              val movie = mappers.movie.fromDatabase(listMovie)
              val translation = moviesTranslations[movie.traktId]
              val rating = moviesRatings.find { it.idTrakt == movie.ids.trakt }
              createListDetailsItem(movie, listItem, translation, rating, isRankSort, listedAt, moviesEnabled, list.sortByLocal)
            }
            else -> throw IllegalStateException("Unsupported list item type.")
          }
        }
      }.awaitAll()

      itemsToDelete.forEach {
        listsRepository.removeFromList(list.id, IdTrakt(it.idTrakt), it.type)
      }

      val sortedItems = sortItems(
        items = items.filterNotNull(),
        sort = list.sortByLocal,
        sortHow = list.sortHowLocal,
        typeFilters = list.filterTypeLocal
      )
      Pair(sortedItems, listItems.count())
    }

  private suspend fun createListDetailsItem(
    movie: Movie,
    listItem: CustomListItem,
    translation: Translation?,
    userRating: TraktRating?,
    isRankSort: Boolean,
    listedAt: ZonedDateTime,
    moviesEnabled: Boolean,
    sortOrder: SortOrder
  ): ListDetailsItem {
    val image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)
    return ListDetailsItem(
      id = listItem.id,
      rank = listItem.rank,
      rankDisplay = listItem.rank.toInt(),
      show = null,
      movie = movie,
      image = image,
      translation = translation,
      userRating = userRating?.rating,
      isLoading = false,
      isRankDisplayed = isRankSort,
      isManageMode = false,
      isEnabled = moviesEnabled,
      isWatched = moviesRepository.myMovies.exists(movie.ids.trakt),
      isWatchlist = moviesRepository.watchlistMovies.exists(movie.ids.trakt),
      listedAt = listedAt,
      sortOrder = sortOrder
    )
  }

  private suspend fun createListDetailsItem(
    show: Show,
    listItem: CustomListItem,
    translation: Translation?,
    userRating: TraktRating?,
    isRankSort: Boolean,
    listedAt: ZonedDateTime,
    sortOrder: SortOrder
  ): ListDetailsItem {
    val image = showImagesProvider.findCachedImage(show, ImageType.POSTER)
    return ListDetailsItem(
      id = listItem.id,
      rank = listItem.rank,
      rankDisplay = listItem.rank.toInt(),
      show = show,
      movie = null,
      image = image,
      translation = translation,
      userRating = userRating?.rating,
      isLoading = false,
      isRankDisplayed = isRankSort,
      isManageMode = false,
      isEnabled = true,
      isWatched = showsRepository.myShows.exists(show.ids.trakt),
      isWatchlist = showsRepository.watchlistShows.exists(show.ids.trakt),
      listedAt = listedAt,
      sortOrder = sortOrder
    )
  }

  fun sortItems(
    items: List<ListDetailsItem>,
    sort: SortOrder,
    sortHow: SortType,
    typeFilters: List<Mode>,
  ) = items
    .filter {
      if (typeFilters.isEmpty()) {
        return@filter true
      }
      when {
        it.isShow() -> typeFilters.contains(SHOWS)
        it.isMovie() -> typeFilters.contains(MOVIES)
        else -> throw IllegalStateException()
      }
    }
    .sortedWith(sorter.sort(sort, sortHow))
    .mapIndexed { index, item ->
      val rankDisplay = if (sortHow == SortType.ASCENDING) index + 1 else items.size - index
      item.copy(
        isRankDisplayed = sort == SortOrder.RANK,
        rankDisplay = rankDisplay,
        sortOrder = sort
      )
    }

  suspend fun deleteListItem(
    listId: Long,
    itemTraktId: IdTrakt,
    itemType: Mode,
  ) {
    listsRepository.removeFromList(listId, itemTraktId, itemType.type)
    val isQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
    if (isQuickRemoveEnabled) {
      quickSyncManager.scheduleRemoveFromList(itemTraktId.id, listId, itemType)
    }
  }
}
