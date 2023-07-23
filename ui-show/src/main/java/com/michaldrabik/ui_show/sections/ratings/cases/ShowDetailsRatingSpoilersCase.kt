package com.michaldrabik.ui_show.sections.ratings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.ShowDetailsUiState.FollowedState
import com.michaldrabik.ui_show.cases.ShowDetailsHiddenCase
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsWatchlistCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRatingSpoilersCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val watchlistCase: ShowDetailsWatchlistCase,
  private val hiddenCase: ShowDetailsHiddenCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
) {

  suspend fun hideSpoilerRatings(
    show: Show,
    ratings: Ratings,
  ): Ratings = withContext(dispatchers.IO) {
    val spoilers = settingsSpoilersRepository.getAll()

    val isMy = async { myShowsCase.isMyShows(show) }
    val isWatchlist = async { watchlistCase.isWatchlist(show) }
    val isHidden = async { hiddenCase.isHidden(show) }

    val state = FollowedState(
      isMyShows = isMy.await(),
      isWatchlist = isWatchlist.await(),
      isHidden = isHidden.await(),
      withAnimation = false
    )

    val isMyHidden = spoilers.isMyShowsRatingsHidden && state.isMyShows
    val isWatchlistHidden = spoilers.isWatchlistShowsRatingsHidden && state.isWatchlist
    val isHiddenHidden = spoilers.isHiddenShowsRatingsHidden && state.isHidden
    val isNotCollectedHidden = spoilers.isNotCollectedShowsRatingsHidden && !state.isInCollection()

    return@withContext ratings.copy(
      isHidden = isMyHidden || isWatchlistHidden || isHiddenHidden || isNotCollectedHidden,
      isTapToReveal = settingsSpoilersRepository.isTapToReveal
    )
  }
}
