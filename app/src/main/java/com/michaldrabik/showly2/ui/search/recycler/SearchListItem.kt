package com.michaldrabik.showly2.ui.search.recycler

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show

data class SearchListItem(
  val show: Show,
  val image: Image
)