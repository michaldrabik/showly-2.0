package com.michaldrabik.ui_movie

import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class MovieDetailsUiState(
  val movie: Movie? = null,
  val movieLoading: Boolean? = null,
  val image: Image? = null,
  val comments: List<Comment>? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val translation: Translation? = null,
  val country: AppCountry? = null,
  val dateFormat: DateTimeFormatter? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
  val isSignedIn: Boolean = false,
  val isPremium: Boolean = false
) {

  data class FollowedState(
    val isMyMovie: Boolean,
    val isWatchlist: Boolean,
    val isHidden: Boolean,
    val withAnimation: Boolean,
  ) {

    companion object {
      fun idle() = FollowedState(isMyMovie = false, isWatchlist = false, isHidden = false, withAnimation = true)
      fun inMyMovies() = FollowedState(isMyMovie = true, isWatchlist = false, isHidden = false, withAnimation = true)
      fun inHidden() = FollowedState(isMyMovie = false, isWatchlist = false, isHidden = true, withAnimation = true)
      fun inWatchlist() = FollowedState(isMyMovie = false, isWatchlist = true, isHidden = false, withAnimation = true)
    }
  }
}
