package com.michaldrabik.ui_comments.post

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Comment

data class PostCommentUiState(
  val isLoading: Boolean = false,
  val isSuccess: ActionEvent<Pair<String, Comment>>? = null,
)
