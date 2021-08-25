package com.michaldrabik.ui_movie

import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.related.RelatedListItem
import java.time.format.DateTimeFormatter

data class MovieDetailsUiState(
  val movie: Movie? = null,
  val movieLoading: Boolean? = null,
  val image: Image? = null,
  val actors: List<Actor>? = null,
  val relatedMovies: List<RelatedListItem>? = null,
  val comments: List<Comment>? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val ratings: Ratings? = null,
  val streamings: StreamingsState? = null,
  val removeFromTraktHistory: ActionEvent<Boolean>? = null,
  val removeFromTraktWatchlist: ActionEvent<Boolean>? = null,
  val showFromTraktLoading: Boolean? = null,
  val translation: Translation? = null,
  val country: AppCountry? = null,
  val dateFormat: DateTimeFormatter? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
  val isSignedIn: Boolean = false,
  val isPremium: Boolean = false,
  val isFinished: ActionEvent<Boolean>? = null,
) {

  data class StreamingsState(
    val streamings: List<StreamingService>,
    val isLocal: Boolean,
  )

  data class FollowedState(
    val isMyMovie: Boolean,
    val isWatchlist: Boolean,
    val withAnimation: Boolean,
  ) {

    companion object {
      fun idle() = FollowedState(isMyMovie = false, isWatchlist = false, withAnimation = true)
      fun inMyMovies() = FollowedState(isMyMovie = true, isWatchlist = false, withAnimation = true)
      fun inWatchlist() = FollowedState(isMyMovie = false, isWatchlist = true, withAnimation = true)
    }
  }
}
