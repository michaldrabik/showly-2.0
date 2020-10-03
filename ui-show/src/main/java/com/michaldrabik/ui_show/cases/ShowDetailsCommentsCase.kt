package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsCommentsCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadComments(show: Show, limit: Int = 30) =
    showsRepository.detailsShow.loadComments(show.ids.trakt, limit)
}
