package com.michaldrabik.showly2.ui.trakt.exports

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktExportViewModel @Inject constructor(
  private val userManager: UserTraktManager
) : BaseViewModel<TraktExportUiModel>() {

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {

      }
    }
  }
}
