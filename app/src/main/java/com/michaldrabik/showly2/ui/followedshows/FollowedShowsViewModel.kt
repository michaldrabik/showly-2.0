package com.michaldrabik.showly2.ui.followedshows

import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject

class FollowedShowsViewModel @Inject constructor(
  private val interactor: FollowedShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

}
