package com.michaldrabik.ui_lists.lists.cases

import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_lists.lists.helpers.ListsItemImage
import com.michaldrabik.ui_lists.lists.helpers.ListsSorter
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class MainListsCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val dateProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val sorter: ListsSorter,
) {

  companion object {
    private const val IMAGES_LIMIT = 3
  }

  suspend fun loadLists(searchQuery: String?) = coroutineScope {
    val lists = listsRepository.loadAll()
    val dateFormat = dateProvider.loadFullDayFormat()
    val sorting = Pair(
      settingsRepository.sorting.listsAllSortOrder,
      settingsRepository.sorting.listsAllSortType
    )

    lists
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sorting.first, sorting.second))
      .map {
        async {
          val items = localSource.customListsItems.getItemsForListImages(it.id, IMAGES_LIMIT)
          val images = mutableListOf<ListsItemImage>()
          val unavailable = ListsItemImage(Image.createUnavailable(POSTER))
          items.forEach { item ->
            images.add(findImage(item) ?: unavailable)
          }
          if (images.size < IMAGES_LIMIT) {
            (images.size..IMAGES_LIMIT).forEach { _ -> images.add(unavailable) }
          }
          ListsItem(it, images, sorting, dateFormat)
        }
      }.awaitAll()
  }

  private fun List<CustomList>.filterByQuery(query: String?) = when {
    query.isNullOrBlank() -> this
    else -> this.filter {
      it.name.contains(query, ignoreCase = true) ||
        it.description?.contains(query, ignoreCase = true) == true
    }
  }

  private suspend fun findImage(item: CustomListItem) =
    when (item.type) {
      SHOWS.type -> {
        val showDb = localSource.shows.getById(item.idTrakt)
        showDb?.let {
          val show = mappers.show.fromDatabase(it)
          val image = showImagesProvider.findCachedImage(show, POSTER)
          ListsItemImage(image, show = show)
        }
      }
      MOVIES.type -> {
        val movieDb = localSource.movies.getById(item.idTrakt)
        movieDb?.let {
          val movie = mappers.movie.fromDatabase(movieDb)
          val image = movieImagesProvider.findCachedImage(movie, POSTER)
          ListsItemImage(image, movie = movie)
        }
      }
      else -> throw IllegalStateException()
    }
}
