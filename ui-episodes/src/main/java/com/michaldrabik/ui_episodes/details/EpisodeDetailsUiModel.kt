package com.michaldrabik.ui_episodes.details

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class EpisodeDetailsUiModel(
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val episodes: List<Episode>? = null,
  val comments: List<Comment>? = null,
  val commentsLoading: Boolean? = null,
  val isSignedIn: Boolean? = null,
  val ratingState: RatingState? = null,
  val ratingChanged: ActionEvent<Boolean>? = null,
  val translation: ActionEvent<Translation>? = null,
  val dateFormat: DateTimeFormatter? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as EpisodeDetailsUiModel).copy(
      image = newModel.image ?: image,
      imageLoading = newModel.imageLoading ?: imageLoading,
      episodes = newModel.episodes?.toList() ?: episodes,
      comments = newModel.comments?.toList() ?: comments,
      commentsLoading = newModel.commentsLoading ?: commentsLoading,
      translation = newModel.translation ?: translation,
      ratingChanged = newModel.ratingChanged ?: ratingChanged,
      isSignedIn = newModel.isSignedIn ?: isSignedIn,
      dateFormat = newModel.dateFormat ?: dateFormat,
      commentsDateFormat = newModel.commentsDateFormat ?: commentsDateFormat,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}
