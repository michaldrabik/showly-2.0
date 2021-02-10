package com.michaldrabik.ui_comments.post

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent

data class PostCommentUiModel(
  val isLoading: Boolean? = null,
  val successEvent: ActionEvent<Boolean>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as PostCommentUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      successEvent = newModel.successEvent ?: successEvent
    )
}
