package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<WatchlistUiModel>() }

  fun loadWatchlist() {
    viewModelScope.launch {
      try {
        val items = interactor.loadWatchlist()
        uiStream.value = WatchlistUiModel(watchlistItems = items)
      } catch (t: Throwable) {
        TODO()
      }
    }
  }
}