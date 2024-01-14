package com.michaldrabik.ui_movie.sections.streamings

import com.michaldrabik.ui_model.StreamingService

data class MovieDetailsStreamingsUiState(
  val streamings: StreamingsState? = null,
) {

  data class StreamingsState(
    val streamings: List<StreamingService>,
    val isLocal: Boolean,
  )
}
