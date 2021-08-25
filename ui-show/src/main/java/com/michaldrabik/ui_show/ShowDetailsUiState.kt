package com.michaldrabik.ui_show

import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.helpers.StreamingsBundle
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.seasons.SeasonListItem
import java.time.format.DateTimeFormatter

data class ShowDetailsUiState(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val seasons: List<SeasonListItem>? = null,
  val actors: List<Actor>? = null,
  val relatedShows: List<RelatedListItem>? = null,
  val streamings: StreamingsBundle? = null,
  val nextEpisode: NextEpisodeBundle? = null,
  val comments: List<Comment>? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState2? = null,
  val ratingState: RatingState? = null,
  val ratings: Ratings? = null,
  val removeFromTraktHistory: ActionEvent<Boolean>? = null,
  val removeFromTraktWatchlist: ActionEvent<Boolean>? = null,
  val showFromTraktLoading: Boolean? = null,
  val translation: Translation? = null,
  val seasonTranslation: ActionEvent<SeasonListItem>? = null,
  val country: AppCountry? = null,
  val isPremium: Boolean = false,
  val isSignedIn: Boolean = false,
  val isFinished: ActionEvent<Boolean>? = null,
) {

  data class FollowedState2(
    val isMyShows: Boolean,
    val isWatchlist: Boolean,
    val isArchived: Boolean,
    val withAnimation: Boolean,
  ) {

    companion object {
      fun inMyShows() = FollowedState2(isMyShows = true, isWatchlist = false, isArchived = false, withAnimation = true)
      fun inWatchlist() = FollowedState2(isMyShows = false, isWatchlist = true, isArchived = false, withAnimation = true)
      fun inArchive() = FollowedState2(isMyShows = false, isWatchlist = false, isArchived = true, withAnimation = true)
      fun notFollowed() = FollowedState2(isMyShows = false, isWatchlist = false, isArchived = false, withAnimation = true)
    }
  }
}
