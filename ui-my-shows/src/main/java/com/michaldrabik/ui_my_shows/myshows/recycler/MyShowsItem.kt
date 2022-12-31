package com.michaldrabik.ui_my_shows.myshows.recycler

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation

data class MyShowsItem(
  val type: Type,
  val header: Header?,
  val recentsSection: RecentsSection?,
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean,
  val translation: Translation? = null,
  val userRating: Int? = null,
  val sortOrder: SortOrder? = null,
) : ListItem {

  enum class Type {
    RECENT_SHOWS,
    ALL_SHOWS_HEADER,
    ALL_SHOWS_ITEM,
  }

  data class Header(
    val section: MyShowsSection,
    val itemCount: Int,
    val sortOrder: Pair<SortOrder, SortType>?,
  )

  data class RecentsSection(
    val items: List<MyShowsItem>,
  )

  companion object {
    fun createHeader(
      section: MyShowsSection,
      itemCount: Int,
      sortOrder: Pair<SortOrder, SortType>?,
    ) = MyShowsItem(
      type = Type.ALL_SHOWS_HEADER,
      header = Header(section, itemCount, sortOrder),
      recentsSection = null,
      show = Show.EMPTY,
      image = Image.createUnavailable(POSTER),
      isLoading = false
    )

    fun createRecentsSection(
      shows: List<MyShowsItem>,
    ) = MyShowsItem(
      type = Type.RECENT_SHOWS,
      header = null,
      recentsSection = RecentsSection(shows),
      show = Show.EMPTY,
      image = Image.createUnavailable(POSTER),
      isLoading = false
    )
  }
}
