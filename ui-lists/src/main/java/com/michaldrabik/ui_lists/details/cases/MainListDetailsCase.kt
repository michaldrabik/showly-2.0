package com.michaldrabik.ui_lists.details.cases

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.CustomListItem
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.SortOrderList
import com.michaldrabik.ui_model.SortOrderList.DATE_ADDED
import com.michaldrabik.ui_model.SortOrderList.NEWEST
import com.michaldrabik.ui_model.SortOrderList.RANK
import com.michaldrabik.ui_model.SortOrderList.RATING
import com.michaldrabik.ui_model.SortOrderList.TITLE
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@AppScope
class MainListDetailsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  private val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadDetails(id: Long) = listsRepository.loadById(id)

  suspend fun loadItems(list: CustomList) = coroutineScope {
    val moviesEnabled = settingsRepository.isMoviesEnabled
    val listItems = listsRepository.loadItemsById(list.id)

    val showsAsync = async {
      database.showsDao().getAll(listItems.filter { it.type == SHOWS.type }.map { it.idTrakt })
    }
    val moviesAsync = async {
      database.moviesDao().getAll(listItems.filter { it.type == MOVIES.type }.map { it.idTrakt })
    }

    val showsTranslationsAsync = async {
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllShowsLocal(language)
    }
    val moviesTranslationsAsync = async {
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)
    }

    val (shows, movies) = Pair(showsAsync.await(), moviesAsync.await())
    val (showsTranslations, moviesTranslations) = Pair(showsTranslationsAsync.await(), moviesTranslationsAsync.await())

    val isRankSort = list.sortByLocal == RANK
    val items = listItems.map { listItem ->
      async {
        val isDragEnabled = false
        val listedAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(listItem.listedAt), ZoneId.of("UTC"))
        when (listItem.type) {
          SHOWS.type -> {
            val show = mappers.show.fromDatabase(shows.first { it.idTrakt == listItem.idTrakt })
            val image = showImagesProvider.findCachedImage(show, ImageType.POSTER)
            val translation = showsTranslations[show.traktId]
            ListDetailsItem(
              id = listItem.id,
              rank = listItem.rank,
              show = show,
              movie = null,
              image = image,
              translation = translation,
              isLoading = false,
              isRankDisplayed = isRankSort,
              isManageMode = isDragEnabled,
              isEnabled = true,
              listedAt = listedAt
            )
          }
          MOVIES.type -> {
            val movie = mappers.movie.fromDatabase(movies.first { it.idTrakt == listItem.idTrakt })
            val image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)
            val translation = moviesTranslations[movie.traktId]
            ListDetailsItem(
              id = listItem.id,
              rank = listItem.rank,
              show = null,
              movie = movie,
              image = image,
              translation = translation,
              isLoading = false,
              isRankDisplayed = isRankSort,
              isManageMode = isDragEnabled,
              isEnabled = moviesEnabled,
              listedAt = listedAt
            )
          }
          else -> throw IllegalStateException("Unsupported list item type.")
        }
      }
    }.awaitAll()

    sortItems(items, list.sortByLocal)
  }

  fun sortItems(items: List<ListDetailsItem>, sort: SortOrderList): List<ListDetailsItem> {
    val sorted = when (sort) {
      RANK -> items.sortedBy { it.rank }
      TITLE -> items.sortedBy {
        val translatedTitle =
          if (it.translation?.hasTitle == false) null
          else it.translation?.title
        translatedTitle ?: it.getTitleNoThe()
      }
      NEWEST -> items.sortedWith(compareByDescending<ListDetailsItem> { it.getYear() }.thenByDescending { it.getDate() })
      RATING -> items.sortedByDescending { it.getRating() }
      DATE_ADDED -> items.sortedByDescending { it.listedAt }
    }
    return sorted.map { it.copy(isRankDisplayed = sort == RANK) }
  }

  suspend fun updateRanks(listId: Long, items: List<ListDetailsItem>): List<ListDetailsItem> {
    val now = nowUtcMillis()
    val listItems = listsRepository.loadItemsById(listId)
    val updateItems = mutableListOf<ListDetailsItem>()
    val updateItemsDb = mutableListOf<CustomListItem>()
    items.forEachIndexed { index, item ->
      val dbItem = listItems.first { it.id == item.id }.copy(rank = index + 1L, updatedAt = now)
      val updatedItem = item.copy(rank = index + 1L)
      updateItems.add(updatedItem)
      updateItemsDb.add(dbItem)
    }
    database.withTransaction {
      database.customListsItemsDao().update(updateItemsDb)
      database.customListsDao().updateTimestamp(listId, now)
    }
    return updateItems
  }

  suspend fun deleteList(listId: Long) = listsRepository.deleteList(listId)
}
