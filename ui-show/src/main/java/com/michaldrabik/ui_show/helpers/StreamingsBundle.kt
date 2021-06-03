package com.michaldrabik.ui_show.helpers

import com.michaldrabik.ui_model.StreamingService

data class StreamingsBundle(
  val streamings: List<StreamingService>,
  val isLocal: Boolean,
)
