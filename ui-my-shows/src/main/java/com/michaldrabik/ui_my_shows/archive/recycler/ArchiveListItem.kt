package com.michaldrabik.ui_my_shows.archive.recycler

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation

sealed class ArchiveListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  data class ShowItem(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val translation: Translation? = null,
    val userRating: Int? = null,
  ) : ArchiveListItem(
    show = show,
    image = image,
    isLoading = isLoading
  )

  data class FiltersItem(
    val sortOrder: SortOrder,
    val sortType: SortType,
  ) : ArchiveListItem(
    show = Show.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false
  )
}
