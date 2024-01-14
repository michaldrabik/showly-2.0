package com.michaldrabik.ui_episodes.details

import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class EpisodeDetailsUiState(
  val image: Image? = null,
  val isImageLoading: Boolean = false,
  val episodes: List<Episode>? = null,
  val comments: List<Comment>? = null,
  val isCommentsLoading: Boolean = false,
  val isSignedIn: Boolean = false,
  val rating: RatingState? = null,
  val translation: Translation? = null,
  val dateFormat: DateTimeFormatter? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
  val spoilers: SpoilersSettings? = null
)
