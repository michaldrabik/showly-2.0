package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsMainCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)
}
