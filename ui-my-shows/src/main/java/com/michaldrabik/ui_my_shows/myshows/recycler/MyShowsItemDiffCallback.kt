package com.michaldrabik.ui_my_shows.myshows.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.HEADER
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.HORIZONTAL_SHOWS
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.RECENT_SHOWS

class MyShowsItemDiffCallback : DiffUtil.ItemCallback<MyShowsItem>() {

  override fun areItemsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    when (oldItem.type) {
      RECENT_SHOWS -> true
      HORIZONTAL_SHOWS -> {
        oldItem.type == newItem.type &&
          oldItem.horizontalSection?.section?.name == newItem.horizontalSection?.section?.name
      }
      else -> oldItem.type == newItem.type && oldItem.show.ids.trakt == newItem.show.ids.trakt
    }

  override fun areContentsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    when (oldItem.type) {
      HEADER -> oldItem.header == newItem.header
      RECENT_SHOWS -> oldItem.recentsSection == newItem.recentsSection
      HORIZONTAL_SHOWS -> oldItem.horizontalSection?.items == newItem.horizontalSection?.items
      else ->
        oldItem.image == newItem.image &&
          oldItem.isLoading == newItem.isLoading &&
          oldItem.translation == newItem.translation &&
          oldItem.userRating == newItem.userRating
    }
}
