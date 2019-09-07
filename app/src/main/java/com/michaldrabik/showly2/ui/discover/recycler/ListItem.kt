package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show

interface ListItem {
  val show: Show
  val image: Image
  val isLoading: Boolean
}