package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

@AppScope
class ShowDetailsMainCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)
}
