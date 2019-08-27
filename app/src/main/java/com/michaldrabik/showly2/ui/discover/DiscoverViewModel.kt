package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val cloud: Cloud
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadTrendingShows() {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(showLoading = true)
      try {
        val shows = cloud.traktApi.fetchTrendingShows()
        delay(2000)
        uiStream.value = DiscoverUiModel(trendingShows = shows)
      } catch (t: Throwable) {
        //TODO Errors
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
      }
    }
  }
}