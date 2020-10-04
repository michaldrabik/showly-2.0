package com.michaldrabik.ui_my_shows.myshows.recycler.section

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem

class MyShowsSectionDiffCallback : DiffUtil.ItemCallback<MyShowsItem>() {

  override fun areItemsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: MyShowsItem, newItem: MyShowsItem) =
    oldItem.image == newItem.image && oldItem.isLoading == newItem.isLoading
}
