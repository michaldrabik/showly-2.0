package com.michaldrabik.ui_my_movies.mymovies

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.utilities.ActionEvent
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
import com.michaldrabik.ui_my_movies.mymovies.cases.MyMoviesRatingsCase
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.ALL_MOVIES_ITEM
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.RECENT_MOVIE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MyMoviesViewModel @Inject constructor(
  private val loadMoviesCase: MyMoviesLoadCase,
  private val ratingsCase: MyMoviesRatingsCase,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<MyMoviesItem>?>(null)
  private val itemsUpdateState = MutableStateFlow<ActionEvent<Boolean>?>(null)

  val uiState = combine(
    itemsState,
    itemsUpdateState
  ) { itemsState, _ ->
    MyMoviesUiState(
      items = itemsState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MyMoviesUiState()
  )

  fun loadMovies(notifyListsUpdate: Boolean = false) {
    viewModelScope.launch {
      val settings = loadMoviesCase.loadSettings()
      val dateFormat = loadMoviesCase.loadDateFormat()
      val movies = loadMoviesCase.loadAll().map { toListItemAsync(ALL_MOVIES_ITEM, it, dateFormat) }.awaitAll()

      val allMovies = loadMoviesCase.filterSectionMovies(movies, ALL)
      val recentMovies = if (settings.myMoviesRecentIsEnabled) {
        loadMoviesCase.loadRecentMovies().map { toListItemAsync(RECENT_MOVIE, it, dateFormat, ImageType.FANART) }.awaitAll()
      } else {
        emptyList()
      }

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

      itemsState.value = listItems
      itemsUpdateState.value = ActionEvent(notifyListsUpdate)

      loadRatings(listItems)
    }
  }

  private fun loadRatings(items: MutableList<MyMoviesItem>) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "MyMoviesViewModel::loadRatings()")
      }
    }
  }

  fun loadSortedSection(section: MyMoviesSection, order: SortOrder) {
    viewModelScope.launch {
      loadMoviesCase.setSectionSortOrder(section, order)
      loadMovies(notifyListsUpdate = true)
    }
  }

  fun loadMissingImage(item: MyMoviesItem, force: Boolean) {
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

  fun loadMissingTranslation(item: MyMoviesItem) {
    if (item.translation != null || loadMoviesCase.language == DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadMoviesCase.loadTranslation(item.movie, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "MyMoviesViewModel::loadMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: MyMoviesItem) {
    val items = uiState.value.items?.toMutableList()
    items?.findReplace(new) { it.isSameAs(new) }
    itemsState.value = items
  }

  private fun CoroutineScope.toListItemAsync(
    itemType: Type,
    movie: Movie,
    dateFormat: DateTimeFormatter,
    type: ImageType = POSTER,
  ) = async {
    val image = loadMoviesCase.findCachedImage(movie, type)
    val translation = loadMoviesCase.loadTranslation(movie, true)
    MyMoviesItem(itemType, null, null, null, movie, image, false, translation, null, dateFormat)
  }
}
