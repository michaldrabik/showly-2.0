package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.MutableLiveData
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<WatchlistUiModel>() }

}