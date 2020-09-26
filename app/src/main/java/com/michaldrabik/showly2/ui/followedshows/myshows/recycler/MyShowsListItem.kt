package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show

data class MyShowsListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false
) : ListItem
