package com.michaldrabik.showly2.ui.show

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem
import com.michaldrabik.showly2.utilities.ActionEvent
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating

data class ShowDetailsUiModel(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val nextEpisode: Episode? = null,
  val actors: List<Actor>? = null,
  val relatedShows: List<RelatedListItem>? = null,
  val seasons: List<SeasonListItem>? = null,
  val comments: List<Comment>? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val removeFromTraktHistory: ActionEvent<Boolean>? = null,
  val removeFromTraktSeeLater: ActionEvent<Boolean>? = null,
  val showFromTraktLoading: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ShowDetailsUiModel).copy(
      show = newModel.show ?: show,
      showLoading = newModel.showLoading ?: showLoading,
      showFromTraktLoading = newModel.showFromTraktLoading ?: showFromTraktLoading,
      image = newModel.image ?: image,
      nextEpisode = newModel.nextEpisode ?: nextEpisode,
      actors = newModel.actors ?: actors,
      relatedShows = newModel.relatedShows ?: relatedShows,
      seasons = newModel.seasons ?: seasons,
      comments = newModel.comments ?: comments,
      followedState = newModel.followedState ?: followedState,
      removeFromTraktHistory = newModel.removeFromTraktHistory ?: removeFromTraktHistory,
      removeFromTraktSeeLater = newModel.removeFromTraktSeeLater ?: removeFromTraktSeeLater,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

data class FollowedState(
  val isMyShows: Boolean,
  val isSeeLater: Boolean,
  val isArchived: Boolean,
  val withAnimation: Boolean
) {

  companion object {
    fun inMyShows() = FollowedState(isMyShows = true, isSeeLater = false, isArchived = false, withAnimation = true)
    fun inSeeLater() = FollowedState(isMyShows = false, isSeeLater = true, isArchived = false, withAnimation = true)
    fun inArchive() = FollowedState(isMyShows = false, isSeeLater = false, isArchived = true, withAnimation = true)
    fun notFollowed() = FollowedState(isMyShows = false, isSeeLater = false, isArchived = false, withAnimation = true)
  }
}

data class RatingState(
  val userRating: TraktRating? = null,
  val rateAllowed: Boolean? = null,
  val rateLoading: Boolean? = null
) {

  fun hasRating() = userRating != null && userRating.rating > 0
}
