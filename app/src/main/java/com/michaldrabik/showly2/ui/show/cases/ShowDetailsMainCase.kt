package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsMainCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)
}
