package com.michaldrabik.showly2.ui.followedshows.seelater.recycler

import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show

data class SeeLaterListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false
) : ListItem
