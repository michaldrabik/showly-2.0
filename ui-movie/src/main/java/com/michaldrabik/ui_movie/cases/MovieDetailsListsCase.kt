package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsListsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val listsRepository: ListsRepository
) {

  suspend fun countLists(movie: Movie) = withContext(dispatchers.IO) {
    listsRepository.loadListIdsForItem(IdTrakt(movie.traktId), Mode.MOVIES.type).size
  }
}
