package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodesLoadShowCase @Inject constructor(
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)
}
