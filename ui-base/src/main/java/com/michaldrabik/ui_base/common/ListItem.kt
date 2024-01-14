package com.michaldrabik.ui_base.common

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show

interface ListItem {
  val show: Show
  val image: Image
  val isLoading: Boolean

  infix fun isSameAs(other: ListItem) = show.ids.trakt == other.show.ids.trakt
}
