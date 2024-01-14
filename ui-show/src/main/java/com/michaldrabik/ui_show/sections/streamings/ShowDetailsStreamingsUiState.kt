package com.michaldrabik.ui_show.sections.streamings

import com.michaldrabik.ui_model.StreamingService

data class ShowDetailsStreamingsUiState(
  val streamings: StreamingsState? = null,
) {

  data class StreamingsState(
    val streamings: List<StreamingService>,
    val isLocal: Boolean,
  )
}
