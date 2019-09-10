package com.michaldrabik.showly2.ui.shows.related

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

data class RelatedListItem(
  override val show: Show,
  override val image: Image,
  override var isLoading: Boolean = false
) : ListItem