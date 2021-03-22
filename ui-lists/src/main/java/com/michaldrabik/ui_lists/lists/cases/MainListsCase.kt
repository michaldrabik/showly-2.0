package com.michaldrabik.ui_lists.lists.cases

import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.CustomListItem
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class MainListsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val dateProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  companion object {
    private const val IMAGES_LIMIT = 3
  }

  suspend fun loadLists() = coroutineScope {
    val lists = listsRepository.loadAll()
    val dateFormat = dateProvider.loadFullDayFormat()
    val sortType = settingsRepository.load().listsSortBy

    lists
      .sortedByType(sortType)
      .map {
        async {
          val items = database.customListsItemsDao().getItemsForListImages(it.id, IMAGES_LIMIT)
          val images = mutableListOf<Image>()
          val unavailable = Image.createUnavailable(POSTER)
          items.forEach { item ->
            images.add(findImage(item) ?: unavailable)
          }
          if (images.size < IMAGES_LIMIT) {
            (images.size..IMAGES_LIMIT).forEach { _ -> images.add(unavailable) }
          }
          ListsItem(it, images, sortType, dateFormat)
        }
      }.awaitAll()
  }

  private suspend fun findImage(item: CustomListItem) =
    when (item.type) {
      SHOWS.type -> {
        val showDb = database.showsDao().getById(item.idTrakt)
        showDb?.let {
          val show = mappers.show.fromDatabase(showDb)
          showImagesProvider.findCachedImage(show, POSTER)
        }
      }
      MOVIES.type -> {
        val movieDb = database.moviesDao().getById(item.idTrakt)
        movieDb?.let {
          val movie = mappers.movie.fromDatabase(movieDb)
          movieImagesProvider.findCachedImage(movie, POSTER)
        }
      }
      else -> throw IllegalStateException()
    }

  private fun List<CustomList>.sortedByType(sortType: SortOrder) =
    when (sortType) {
      SortOrder.NAME -> this.sortedBy { it.name }
      SortOrder.NEWEST -> this.sortedByDescending { it.createdAt }
      SortOrder.DATE_UPDATED -> this.sortedByDescending { it.updatedAt }
      else -> error("Should not be used here.")
    }
}
