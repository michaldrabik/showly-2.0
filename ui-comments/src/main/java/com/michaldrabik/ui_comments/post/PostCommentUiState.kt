package com.michaldrabik.ui_comments.post

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Comment

data class PostCommentUiState(
  val isLoading: Boolean = false,
  val isSuccess: Event<Pair<String, Comment>>? = null,
)
