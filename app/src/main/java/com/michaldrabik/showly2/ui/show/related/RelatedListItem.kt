package com.michaldrabik.showly2.ui.show.related

import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show

data class RelatedListItem(
  override val show: Show,
  override val image: Image,
  override var isLoading: Boolean = false
) : ListItem
