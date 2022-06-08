package com.michaldrabik.ui_show

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.helpers.ShowDetailsMeta

data class ShowDetailsUiState(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val translation: Translation? = null,
  val meta: ShowDetailsMeta? = null
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
