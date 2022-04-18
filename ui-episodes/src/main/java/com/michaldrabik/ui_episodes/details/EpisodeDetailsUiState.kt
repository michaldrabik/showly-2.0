package com.michaldrabik.ui_episodes.details

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class EpisodeDetailsUiState(
  val image: Image? = null,
  val isImageLoading: Boolean = false,
  val episodes: List<Episode>? = null,
  val comments: List<Comment>? = null,
  val isCommentsLoading: Boolean = false,
  val isSignedIn: Boolean = false,
  val ratingState: RatingState? = null,
  val translation: Event<Translation>? = null,
  val dateFormat: DateTimeFormatter? = null,
  val commentsDateFormat: DateTimeFormatter? = null,
)
