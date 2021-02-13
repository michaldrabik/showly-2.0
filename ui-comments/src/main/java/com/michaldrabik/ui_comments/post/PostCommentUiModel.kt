package com.michaldrabik.ui_comments.post

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Comment

data class PostCommentUiModel(
  val isLoading: Boolean? = null,
  val successEvent: ActionEvent<Pair<String, Comment>>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as PostCommentUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      successEvent = newModel.successEvent ?: successEvent
    )
}
