package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.ui_model.SpoilersSettings
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsSpoilersRepository @Inject constructor(
  @Named("spoilersPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val SHOWS_UNCOLLECTED_SHOWS_HIDDEN = "SHOWS_UNCOLLECTED_SHOWS_HIDDEN"
    private const val SHOWS_UNCOLLECTED_SHOWS_RATINGS_HIDDEN = "SHOWS_UNCOLLECTED_SHOWS_RATINGS_HIDDEN"
    private const val SHOWS_MY_SHOWS_HIDDEN = "SHOWS_MY_SHOWS_HIDDEN"
    private const val SHOWS_MY_SHOWS_RATINGS_HIDDEN = "SHOWS_MY_SHOWS_RATINGS_HIDDEN"
    private const val SHOWS_WATCHLIST_SHOWS_HIDDEN = "SHOWS_WATCHLIST_SHOWS_HIDDEN"
    private const val SHOWS_WATCHLIST_SHOWS_RATINGS_HIDDEN = "SHOWS_WATCHLIST_SHOWS_RATINGS_HIDDEN"
    private const val SHOWS_HIDDEN_SHOWS_HIDDEN = "SHOWS_HIDDEN_SHOWS_HIDDEN"
    private const val SHOWS_HIDDEN_SHOWS_RATINGS_HIDDEN = "SHOWS_HIDDEN_SHOWS_RATINGS_HIDDEN"

    private const val MOVIES_UNCOLLECTED_MOVIES_HIDDEN = "MOVIES_UNCOLLECTED_MOVIES_HIDDEN"
    private const val MOVIES_UNCOLLECTED_MOVIES_RATINGS_HIDDEN = "MOVIES_UNCOLLECTED_MOVIES_RATINGS_HIDDEN"
    private const val MOVIES_MY_MOVIES_HIDDEN = "MOVIES_MY_MOVIES_HIDDEN"
    private const val MOVIES_MY_MOVIES_RATINGS_HIDDEN = "MOVIES_MY_MOVIES_RATINGS_HIDDEN"
    private const val MOVIES_WATCHLIST_MOVIES_HIDDEN = "MOVIES_WATCHLIST_MOVIES_HIDDEN"
    private const val MOVIES_WATCHLIST_MOVIES_RATINGS_HIDDEN = "MOVIES_WATCHLIST_MOVIES_RATINGS_HIDDEN"
    private const val MOVIES_HIDDEN_MOVIES_HIDDEN = "MOVIES_HIDDEN_MOVIES_HIDDEN"
    private const val MOVIES_HIDDEN_MOVIES_RATINGS_HIDDEN = "MOVIES_HIDDEN_MOVIES_RATINGS_HIDDEN"

    private const val EPISODES_TITLE_HIDDEN = "EPISODES_TITLE_HIDDEN"
    private const val EPISODES_DESCRIPTION_HIDDEN = "EPISODES_DESCRIPTION_HIDDEN"
    private const val EPISODES_RATING_HIDDEN = "EPISODES_RATING_HIDDEN"
    private const val EPISODES_IMAGE_HIDDEN = "EPISODES_IMAGE_HIDDEN"

    private const val TAP_TO_REVEAL = "TAP_TO_REVEAL"
  }

  var isMyShowsHidden by BooleanPreference(preferences, SHOWS_MY_SHOWS_HIDDEN, false)
  var isMyShowsRatingsHidden by BooleanPreference(preferences, SHOWS_MY_SHOWS_RATINGS_HIDDEN, false)
  var isWatchlistShowsHidden by BooleanPreference(preferences, SHOWS_WATCHLIST_SHOWS_HIDDEN, false)
  var isWatchlistShowsRatingsHidden by BooleanPreference(preferences, SHOWS_WATCHLIST_SHOWS_RATINGS_HIDDEN, false)
  var isHiddenShowsHidden by BooleanPreference(preferences, SHOWS_HIDDEN_SHOWS_HIDDEN, false)
  var isHiddenShowsRatingsHidden by BooleanPreference(preferences, SHOWS_HIDDEN_SHOWS_RATINGS_HIDDEN, false)
  var isUncollectedShowsHidden by BooleanPreference(preferences, SHOWS_UNCOLLECTED_SHOWS_HIDDEN, false)
  var isUncollectedShowsRatingsHidden by BooleanPreference(preferences, SHOWS_UNCOLLECTED_SHOWS_RATINGS_HIDDEN, false)

  var isMyMoviesHidden by BooleanPreference(preferences, MOVIES_MY_MOVIES_HIDDEN, false)
  var isMyMoviesRatingsHidden by BooleanPreference(preferences, MOVIES_MY_MOVIES_RATINGS_HIDDEN, false)
  var isWatchlistMoviesHidden by BooleanPreference(preferences, MOVIES_WATCHLIST_MOVIES_HIDDEN, false)
  var isWatchlistMoviesRatingsHidden by BooleanPreference(preferences, MOVIES_WATCHLIST_MOVIES_RATINGS_HIDDEN, false)
  var isHiddenMoviesHidden by BooleanPreference(preferences, MOVIES_HIDDEN_MOVIES_HIDDEN, false)
  var isHiddenMoviesRatingsHidden by BooleanPreference(preferences, MOVIES_HIDDEN_MOVIES_RATINGS_HIDDEN, false)
  var isUncollectedMoviesHidden by BooleanPreference(preferences, MOVIES_UNCOLLECTED_MOVIES_HIDDEN, false)
  var isUncollectedMoviesRatingsHidden by BooleanPreference(preferences, MOVIES_UNCOLLECTED_MOVIES_RATINGS_HIDDEN, false)

  var isEpisodesTitleHidden by BooleanPreference(preferences, EPISODES_TITLE_HIDDEN, false)
  var isEpisodesDescriptionHidden by BooleanPreference(preferences, EPISODES_DESCRIPTION_HIDDEN, false)
  var isEpisodesRatingHidden by BooleanPreference(preferences, EPISODES_RATING_HIDDEN, false)
  var isEpisodesImageHidden by BooleanPreference(preferences, EPISODES_IMAGE_HIDDEN, false)

  var isTapToReveal by BooleanPreference(preferences, TAP_TO_REVEAL, false)

  fun getAll(): SpoilersSettings = SpoilersSettings(
    isMyShowsHidden = isMyShowsHidden,
    isMyShowsRatingsHidden = isMyShowsRatingsHidden,
    isMyMoviesHidden = isMyMoviesHidden,
    isMyMoviesRatingsHidden = isMyMoviesRatingsHidden,
    isWatchlistShowsHidden = isWatchlistShowsHidden,
    isWatchlistShowsRatingsHidden = isWatchlistShowsRatingsHidden,
    isWatchlistMoviesHidden = isWatchlistMoviesHidden,
    isWatchlistMoviesRatingsHidden = isWatchlistMoviesRatingsHidden,
    isHiddenShowsHidden = isHiddenShowsHidden,
    isHiddenShowsRatingsHidden = isHiddenShowsRatingsHidden,
    isHiddenMoviesHidden = isHiddenMoviesHidden,
    isHiddenMoviesRatingsHidden = isHiddenMoviesRatingsHidden,
    isNotCollectedShowsHidden = isUncollectedShowsHidden,
    isNotCollectedShowsRatingsHidden = isUncollectedShowsRatingsHidden,
    isNotCollectedMoviesHidden = isUncollectedMoviesHidden,
    isNotCollectedMoviesRatingsHidden = isUncollectedMoviesRatingsHidden,
    isEpisodeTitleHidden = isEpisodesTitleHidden,
    isEpisodeDescriptionHidden = isEpisodesDescriptionHidden,
    isEpisodeRatingHidden = isEpisodesRatingHidden,
    isEpisodeImageHidden = isEpisodesImageHidden,
    isTapToReveal = isTapToReveal
  )
}
