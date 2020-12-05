package com.michaldrabik.ui_my_movies.mymovies

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyMoviesSection.ALL
import com.michaldrabik.ui_model.MyMoviesSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.mymovies.cases.MyMoviesLoadCase
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.ALL_MOVIES_ITEM
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.RECENT_MOVIE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyMoviesViewModel @Inject constructor(
  private val loadMoviesCase: MyMoviesLoadCase
) : BaseViewModel<MyMoviesUiModel>() {

  fun loadMovies(notifyListsUpdate: Boolean = false) {
    viewModelScope.launch {
      val movies = loadMoviesCase.loadAll().map { toListItemAsync(ALL_MOVIES_ITEM, it) }.awaitAll()

      val allMovies = loadMoviesCase.filterSectionMovies(movies, ALL)
      val recentMovies = loadMoviesCase.loadRecentMovies().map { toListItemAsync(RECENT_MOVIE, it, ImageType.FANART) }.awaitAll()

      val listItems = mutableListOf<MyMoviesItem>()
      listItems.run {
        if (recentMovies.isNotEmpty()) {
          add(MyMoviesItem.createHeader(RECENTS, recentMovies.count(), null))
          add(MyMoviesItem.createRecentsSection(recentMovies))
        }
        if (allMovies.isNotEmpty()) {
          add(MyMoviesItem.createHeader(ALL, allMovies.count(), loadMoviesCase.loadSortOrder(ALL)))
          addAll(allMovies)
        }
      }

      uiState = MyMoviesUiModel(listItems = listItems, notifyListsUpdate = notifyListsUpdate)
    }
  }

  fun loadSortedSection(section: MyMoviesSection, order: SortOrder) {
    viewModelScope.launch {
      loadMoviesCase.setSectionSortOrder(section, order)
      loadMovies(notifyListsUpdate = true)
    }
  }

  fun loadMissingImage(item: MyMoviesItem, force: Boolean) {

    fun updateItem(new: MyMoviesItem) {
      val items = uiState?.listItems?.toMutableList() ?: mutableListOf()
      items.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(listItems = items)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = loadMoviesCase.loadMissingImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadSectionMissingItem(item: MyMoviesItem, itemSection: MyMoviesItem.HorizontalSection, force: Boolean) {

    fun updateItem(newItem: MyMoviesItem, newSection: MyMoviesItem.HorizontalSection) {
      val items = uiState?.listItems?.toMutableList() ?: mutableListOf()
      val section = items.find { it.horizontalSection?.section == newSection.section }?.horizontalSection

      val sectionItems = section?.items?.toMutableList() ?: mutableListOf()
      sectionItems.findReplace(newItem) { it.isSameAs(newItem) }

      val newSecWithItems = section?.copy(items = sectionItems)
      items.findReplace(newItem.copy(horizontalSection = newSecWithItems)) { it.horizontalSection?.section == newSection.section }

      uiState = uiState?.copy(listItems = items)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true), itemSection)
      try {
        val image = loadMoviesCase.loadMissingImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image), itemSection)
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)), itemSection)
      }
    }
  }

  private fun CoroutineScope.toListItemAsync(
    itemType: Type,
    movie: Movie,
    type: ImageType = POSTER
  ) = async {
    val image = loadMoviesCase.findCachedImage(movie, type)
    val translation = loadMoviesCase.loadTranslation(movie)
    MyMoviesItem(itemType, null, null, null, movie, image, false, translation)
  }
}
