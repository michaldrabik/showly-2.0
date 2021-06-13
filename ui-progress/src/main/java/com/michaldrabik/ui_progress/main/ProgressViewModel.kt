package com.michaldrabik.ui_progress.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.cases.ProgressEpisodesCase
import com.michaldrabik.ui_progress.main.cases.ProgressLoadItemsCase
import com.michaldrabik.ui_progress.main.cases.ProgressPinnedItemsCase
import com.michaldrabik.ui_progress.main.cases.ProgressSettingsCase
import com.michaldrabik.ui_progress.main.cases.ProgressSortOrderCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
  private val loadItemsCase: ProgressLoadItemsCase,
  private val pinnedItemsCase: ProgressPinnedItemsCase,
  private val sortOrderCase: ProgressSortOrderCase,
  private val episodesCase: ProgressEpisodesCase,
  private val settingsCase: ProgressSettingsCase,
  private val imagesProvider: ShowImagesProvider,
) : BaseViewModel<ProgressUiModel>() {

  private var searchQuery = ""

  fun loadProgress(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val shows = loadItemsCase.loadMyShows()
      val dateFormat = loadItemsCase.loadDateFormat()
      val upcomingEnabled = settingsCase.isUpcomingEnabled()
      val progressType = settingsCase.getProgressType()

      val items = shows.map { show ->
        async {
          val item = loadItemsCase.loadProgressItem(show, progressType)
          val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
          item.copy(image = image, dateFormat = dateFormat)
        }
      }.awaitAll()

      val sortOrder = sortOrderCase.loadSortOrder()
      val allItems = loadItemsCase.prepareItems(items, searchQuery, sortOrder)
      uiState = ProgressUiModel(
        items = allItems,
        searchQuery = searchQuery,
        isUpcomingEnabled = upcomingEnabled,
        sortOrder = sortOrder,
        resetScroll = ActionEvent(resetScroll),
      )
    }
  }

  fun onSearchQuery(searchQuery: String) {
    this.searchQuery = searchQuery.trim()
    loadProgress()
  }

  fun setWatchedEpisode(context: Context, bundle: EpisodeBundle) {
    viewModelScope.launch {
      if (!bundle.episode.hasAired(bundle.season)) {
        _messageLiveData.value = MessageEvent.info(R.string.errorEpisodeNotAired)
        return@launch
      }
      episodesCase.setEpisodeWatched(context, bundle)
      loadProgress()
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadProgress(resetScroll = true)
    }
  }

  fun togglePinItem(item: ProgressItem) {
    if (item.isPinned) {
      pinnedItemsCase.removePinnedItem(item)
    } else {
      pinnedItemsCase.addPinnedItem(item)
    }
    loadProgress()
  }
}
