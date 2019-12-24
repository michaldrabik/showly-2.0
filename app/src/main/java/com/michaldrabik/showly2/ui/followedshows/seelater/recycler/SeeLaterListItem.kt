package com.michaldrabik.showly2.ui.followedshows.seelater.recycler

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

data class SeeLaterListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false
) : ListItem
