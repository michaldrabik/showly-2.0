package com.michaldrabik.ui_my_shows.seelater.recycler

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation

data class SeeLaterListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
  val translation: Translation? = null,
) : ListItem
