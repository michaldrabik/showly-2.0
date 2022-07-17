package com.michaldrabik.ui_news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.cases.NewsFiltersCase
import com.michaldrabik.ui_news.cases.NewsLoadItemsCase
import com.michaldrabik.ui_news.cases.NewsViewTypeCase
import com.michaldrabik.ui_news.recycler.NewsListItem
import com.michaldrabik.ui_news.views.item.NewsItemViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
  private val loadNewsCase: NewsLoadItemsCase,
  private val filtersCase: NewsFiltersCase,
  private val viewTypeCase: NewsViewTypeCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var previousRefresh = 0L

  private val itemsState = MutableStateFlow(emptyList<NewsListItem>())
  private val filtersState = MutableStateFlow(emptyList<NewsItem.Type>())
  private val viewTypeState = MutableStateFlow(NewsItemViewType.ROW)
  private val loadingState = MutableStateFlow(false)

  init {
    viewTypeState.value = viewTypeCase.loadViewType()
    loadItems(filters = filtersCase.loadFilters())
  }

  fun loadItems(
    forceRefresh: Boolean = false,
    filters: List<NewsItem.Type>? = null,
  ) {
    if (filters != null) {
      filtersCase.saveFilters(filters.toList())
    }

    if (forceRefresh) {
      loadingState.value = true
    }

    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        loadingState.value = true
      }
      try {
        if (forceRefresh && nowUtcMillis() - previousRefresh < TimeUnit.SECONDS.toMillis(30)) {
          loadingState.value = false
          return@launch
        }

        if (forceRefresh) {
          loadingState.value = true
        } else {
          val currentFilters = filtersCase.loadFilters()
          val cachedItems = loadNewsCase.preloadItems(currentFilters)
          itemsState.value = cachedItems
          filtersState.value = currentFilters
        }

        val currentFilters = filtersCase.loadFilters()
        val items = loadNewsCase.loadItems(forceRefresh, currentFilters)
        itemsState.value = items
        filtersState.value = currentFilters
        loadingState.value = false

        if (forceRefresh) {
          previousRefresh = nowUtcMillis()
        }
      } catch (error: Throwable) {
        Timber.e(error)
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        loadingState.value = false
        rethrowCancellation(error)
      } finally {
        progressJob.cancel()
      }
    }
  }

  fun toggleViewType() {
    viewModelScope.launch {
      val newType = viewTypeCase.toggleViewType()
      viewTypeState.value = newType
    }
  }

  val uiState = combine(
    itemsState,
    filtersState, viewTypeState,
    loadingState
  ) { items, filters, viewType, loading ->
    NewsUiState(
      items = items,
      filters = filters,
      viewType = viewType,
      isLoading = loading
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = NewsUiState()
  )
}
