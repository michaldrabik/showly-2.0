package com.michaldrabik.showly2.discover

import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val cloud: Cloud
) : BaseViewModel() {

  fun loadTrendingShows() {
    viewModelScope.launch {
      val trendingShows = cloud.traktApi.fetchTrendingShows()
    }
  }

}