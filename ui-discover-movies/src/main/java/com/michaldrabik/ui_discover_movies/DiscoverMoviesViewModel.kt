package com.michaldrabik.ui_discover_movies

import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject

class DiscoverMoviesViewModel @Inject constructor(
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<DiscoverMoviesUiModel>() {

  private fun onError(error: Throwable) {
    if (error !is CancellationException) {
      _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadDiscover)
      Timber.e(error)
    }
  }
}
