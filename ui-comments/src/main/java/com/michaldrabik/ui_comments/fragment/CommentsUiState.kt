package com.michaldrabik.ui_comments.fragment

import com.michaldrabik.ui_model.Comment
import java.time.format.DateTimeFormatter

data class CommentsUiState(
  val comments: List<Comment>? = null,
  val dateFormat: DateTimeFormatter? = null,
  val isLoading: Boolean = false,
  val isSignedIn: Boolean = false
)
