package com.michaldrabik.ui_movie

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.related.RelatedListItem

data class MovieDetailsUiModel(
  val movie: Movie? = null,
  val movieLoading: Boolean? = null,
  val image: Image? = null,
  val actors: List<Actor>? = null,
  val relatedMovies: List<RelatedListItem>? = null,
  val comments: List<Comment>? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val removeFromTraktHistory: ActionEvent<Boolean>? = null,
  val removeFromTraktWatchlist: ActionEvent<Boolean>? = null,
  val showFromTraktLoading: Boolean? = null,
  val translation: Translation? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MovieDetailsUiModel).copy(
      movie = newModel.movie ?: movie,
      movieLoading = newModel.movieLoading ?: movieLoading,
      showFromTraktLoading = newModel.showFromTraktLoading ?: showFromTraktLoading,
      image = newModel.image ?: image,
      actors = newModel.actors ?: actors,
      relatedMovies = newModel.relatedMovies ?: relatedMovies,
      comments = newModel.comments ?: comments,
      followedState = newModel.followedState ?: followedState,
      removeFromTraktHistory = newModel.removeFromTraktHistory ?: removeFromTraktHistory,
      removeFromTraktWatchlist = newModel.removeFromTraktWatchlist ?: removeFromTraktWatchlist,
      translation = newModel.translation ?: translation,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

data class FollowedState(
  val isMyMovie: Boolean,
  val isWatchlist: Boolean,
  val isArchived: Boolean,
  val withAnimation: Boolean
) {

  companion object {
    fun inMyMovie() = FollowedState(isMyMovie = true, isWatchlist = false, isArchived = false, withAnimation = true)
    fun inWatchlist() = FollowedState(isMyMovie = false, isWatchlist = true, isArchived = false, withAnimation = true)
    fun inArchive() = FollowedState(isMyMovie = false, isWatchlist = false, isArchived = true, withAnimation = true)
    fun notFollowed() = FollowedState(isMyMovie = false, isWatchlist = false, isArchived = false, withAnimation = true)
  }
}
