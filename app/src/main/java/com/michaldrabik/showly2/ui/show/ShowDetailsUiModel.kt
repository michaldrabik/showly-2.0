package com.michaldrabik.showly2.ui.show

import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.model.Comment
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.TraktRating
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem

data class ShowDetailsUiModel(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val nextEpisode: Episode? = null,
  val actors: List<Actor>? = null,
  val relatedShows: List<RelatedListItem>? = null,
  val seasons: List<SeasonListItem>? = null,
  val comments: List<Comment>? = null,
  val isFollowed: FollowedState? = null,
  val ratingState: RatingState? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ShowDetailsUiModel).copy(
      show = newModel.show ?: show,
      showLoading = newModel.showLoading ?: showLoading,
      image = newModel.image ?: image,
      nextEpisode = newModel.nextEpisode ?: nextEpisode,
      actors = newModel.actors ?: actors,
      relatedShows = newModel.relatedShows ?: relatedShows,
      seasons = newModel.seasons ?: seasons,
      comments = newModel.comments ?: comments,
      isFollowed = newModel.isFollowed ?: isFollowed,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

data class FollowedState(
  val isMyShows: Boolean,
  val isWatchLater: Boolean,
  val withAnimation: Boolean
)

data class RatingState(
  val userRating: TraktRating? = null,
  val rateAllowed: Boolean? = null,
  val rateLoading: Boolean? = null
) {

  fun hasRating() = userRating != null && userRating.rating > 0
}
