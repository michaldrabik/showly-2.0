package com.michaldrabik.ui_show

import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.helpers.StreamingsBundle
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.seasons.SeasonListItem

data class ShowDetailsUiState(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val seasons: List<SeasonListItem>? = null,
  val actors: List<Person>? = null,
  val crew: Map<Person.Department, List<Person>>? = null,
  val relatedShows: List<RelatedListItem>? = null,
  val streamings: StreamingsBundle? = null,
  val nextEpisode: NextEpisodeBundle? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val ratings: Ratings? = null,
  val translation: Translation? = null,
  val country: AppCountry? = null,
  val isPremium: Boolean = false,
  val isSignedIn: Boolean = false
) {

  data class FollowedState(
    val isMyShows: Boolean,
    val isWatchlist: Boolean,
    val isHidden: Boolean,
    val withAnimation: Boolean,
  ) {

    companion object {
      fun idle() = FollowedState(isMyShows = false, isWatchlist = false, isHidden = false, withAnimation = true)
      fun inMyShows() = FollowedState(isMyShows = true, isWatchlist = false, isHidden = false, withAnimation = true)
      fun inWatchlist() = FollowedState(isMyShows = false, isWatchlist = true, isHidden = false, withAnimation = true)
      fun inHidden() = FollowedState(isMyShows = false, isWatchlist = false, isHidden = true, withAnimation = true)
    }
  }
}
