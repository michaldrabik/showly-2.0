package com.michaldrabik.ui_my_shows.myshows.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.ALL_SHOWS_HEADER
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.RECENT_SHOWS

class MyShowsItemDiffCallback : DiffUtil.ItemCallback<MyShowsItem>() {

  override fun areItemsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    when (oldItem.type) {
      RECENT_SHOWS -> true
      else -> oldItem.type == newItem.type && oldItem.show.ids.trakt == newItem.show.ids.trakt
    }

  override fun areContentsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    when (oldItem.type) {
      ALL_SHOWS_HEADER -> oldItem.header == newItem.header
      RECENT_SHOWS -> oldItem.recentsSection == newItem.recentsSection
      else ->
        oldItem.image == newItem.image &&
          oldItem.isLoading == newItem.isLoading &&
          oldItem.translation == newItem.translation &&
          oldItem.userRating == newItem.userRating &&
          oldItem.sortOrder == newItem.sortOrder
    }
}
