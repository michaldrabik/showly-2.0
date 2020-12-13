package com.michaldrabik.ui_my_shows.archive.recycler

import androidx.recyclerview.widget.DiffUtil

class ArchiveDiffCallback : DiffUtil.ItemCallback<ArchiveListItem>() {

  override fun areItemsTheSame(oldItem: ArchiveListItem, newItem: ArchiveListItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: ArchiveListItem, newItem: ArchiveListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
}
