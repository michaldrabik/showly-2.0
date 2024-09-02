package com.michaldrabik.ui_settings.sections.spoilers.helpers

import com.michaldrabik.ui_model.SpoilersSettings
import javax.inject.Inject

class SettingsSpoilersHelper @Inject constructor() {

  fun hasActiveShowsSettings(settings: SpoilersSettings): Boolean =
    settings.isMyShowsHidden ||
      settings.isWatchlistShowsHidden ||
      settings.isHiddenShowsHidden ||
      settings.isNotCollectedShowsHidden ||
      settings.isMyShowsRatingsHidden ||
      settings.isWatchlistShowsRatingsHidden ||
      settings.isHiddenShowsRatingsHidden ||
      settings.isNotCollectedShowsRatingsHidden

  fun hasActiveMoviesSettings(settings: SpoilersSettings): Boolean =
    settings.isMyMoviesHidden ||
      settings.isWatchlistMoviesHidden ||
      settings.isHiddenMoviesHidden ||
      settings.isNotCollectedMoviesHidden ||
      settings.isMyMoviesRatingsHidden ||
      settings.isWatchlistMoviesRatingsHidden ||
      settings.isHiddenMoviesRatingsHidden ||
      settings.isNotCollectedMoviesRatingsHidden

  fun hasActiveEpisodesSettings(settings: SpoilersSettings): Boolean =
    settings.isEpisodeDescriptionHidden ||
      settings.isEpisodeImageHidden ||
      settings.isEpisodeRatingHidden ||
      settings.isEpisodeTitleHidden
}
