package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.UiModel

data class EpisodeDetailsUiModel(
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val comments: List<Comment>? = null,
  val commentsLoading: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as EpisodeDetailsUiModel).copy(
      image = newModel.image ?: image,
      imageLoading = newModel.imageLoading ?: imageLoading,
      comments = newModel.comments ?: comments,
      commentsLoading = newModel.commentsLoading ?: commentsLoading
    )
}