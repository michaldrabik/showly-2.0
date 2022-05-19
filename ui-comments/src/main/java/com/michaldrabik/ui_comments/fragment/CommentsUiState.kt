package com.michaldrabik.ui_comments.fragment

import com.michaldrabik.ui_model.Comment

data class CommentsUiState(
  val comments: List<Comment>? = null,
  val isLoading: Boolean = false
)
