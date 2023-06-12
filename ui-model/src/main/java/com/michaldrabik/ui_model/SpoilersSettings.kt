package com.michaldrabik.ui_model

data class SpoilersSettings(
  val isNotCollectedShowsHidden: Boolean,
  val isNotCollectedShowsRatingsHidden: Boolean,
  val isMyShowsHidden: Boolean,
  val isMyShowsRatingsHidden: Boolean,
  val isWatchlistShowsHidden: Boolean,
  val isWatchlistShowsRatingsHidden: Boolean,
  val isHiddenShowsHidden: Boolean,
  val isHiddenShowsRatingsHidden: Boolean,
  val isNotCollectedMoviesHidden: Boolean,
  val isNotCollectedMoviesRatingsHidden: Boolean,
  val isMyMoviesHidden: Boolean,
  val isMyMoviesRatingsHidden: Boolean,
  val isWatchlistMoviesHidden: Boolean,
  val isWatchlistMoviesRatingsHidden: Boolean,
  val isHiddenMoviesHidden: Boolean,
  val isHiddenMoviesRatingsHidden: Boolean,
) {

  companion object {
    val INITIAL = SpoilersSettings(
      isNotCollectedShowsHidden = false,
      isNotCollectedShowsRatingsHidden = false,
      isMyShowsHidden = false,
      isMyShowsRatingsHidden = false,
      isWatchlistShowsHidden = false,
      isWatchlistShowsRatingsHidden = false,
      isHiddenShowsHidden = false,
      isHiddenShowsRatingsHidden = false,
      isNotCollectedMoviesHidden = false,
      isNotCollectedMoviesRatingsHidden = false,
      isMyMoviesHidden = false,
      isMyMoviesRatingsHidden = false,
      isWatchlistMoviesHidden = false,
      isWatchlistMoviesRatingsHidden = false,
      isHiddenMoviesHidden = false,
      isHiddenMoviesRatingsHidden = false,
    )
  }
}
