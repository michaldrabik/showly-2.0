package com.michaldrabik.showly2.ui.trakt

import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportViewModel @Inject constructor(
  private val cloud: Cloud,
  private val userTraktManager: UserTraktManager
) : BaseViewModel<TraktImportUiModel>() {

  fun startImport() {
    viewModelScope.launch {
      val authToken = userTraktManager.checkAuthorization()
      val results = cloud.traktApi.fetchSyncWatched(authToken.token)
      results.toString()
    }
  }
}
