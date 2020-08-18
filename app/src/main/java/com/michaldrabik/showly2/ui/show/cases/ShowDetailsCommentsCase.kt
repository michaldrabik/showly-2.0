package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsCommentsCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadComments(show: Show, limit: Int = 30) =
    showsRepository.detailsShow.loadComments(show.ids.trakt, limit)
}
