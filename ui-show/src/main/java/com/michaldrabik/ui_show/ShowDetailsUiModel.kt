package com.michaldrabik.ui_show

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.seasons.SeasonListItem
import java.time.format.DateTimeFormatter

data class ShowDetailsUiModel(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val comments: List<Comment>? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val ratings: Ratings? = null,
  val removeFromTraktHistory: ActionEvent<Boolean>? = null,
  val removeFromTraktWatchlist: ActionEvent<Boolean>? = null,
  val showFromTraktLoading: Boolean? = null,
  val translation: Translation? = null,
  val seasonTranslation: ActionEvent<SeasonListItem>? = null,
  val country: AppCountry? = null,
  val isPremium: Boolean? = null,
  val isSignedIn: Boolean? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ShowDetailsUiModel).copy(
      show = newModel.show ?: show,
      showLoading = newModel.showLoading ?: showLoading,
      showFromTraktLoading = newModel.showFromTraktLoading ?: showFromTraktLoading,
      image = newModel.image ?: image,
      comments = newModel.comments ?: comments,
      listsCount = newModel.listsCount ?: listsCount,
      followedState = newModel.followedState ?: followedState,
      removeFromTraktHistory = newModel.removeFromTraktHistory ?: removeFromTraktHistory,
      removeFromTraktWatchlist = newModel.removeFromTraktWatchlist ?: removeFromTraktWatchlist,
      translation = newModel.translation ?: translation,
      seasonTranslation = newModel.seasonTranslation ?: seasonTranslation,
      country = newModel.country ?: country,
      isPremium = newModel.isPremium ?: isPremium,
      isSignedIn = newModel.isSignedIn ?: isSignedIn,
      commentsDateFormat = newModel.commentsDateFormat ?: commentsDateFormat,
      ratings = newModel.ratings ?: ratings,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

data class FollowedState(
  val isMyShows: Boolean,
  val isWatchlist: Boolean,
  val isArchived: Boolean,
  val withAnimation: Boolean,
) {

  companion object {
    fun inMyShows() = FollowedState(isMyShows = true, isWatchlist = false, isArchived = false, withAnimation = true)
    fun inWatchlist() = FollowedState(isMyShows = false, isWatchlist = true, isArchived = false, withAnimation = true)
    fun inArchive() = FollowedState(isMyShows = false, isWatchlist = false, isArchived = true, withAnimation = true)
    fun notFollowed() = FollowedState(isMyShows = false, isWatchlist = false, isArchived = false, withAnimation = true)
  }
}
