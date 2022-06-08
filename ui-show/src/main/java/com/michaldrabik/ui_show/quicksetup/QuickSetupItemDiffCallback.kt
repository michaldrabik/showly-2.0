package com.michaldrabik.ui_show.quicksetup

import androidx.recyclerview.widget.DiffUtil

class QuickSetupItemDiffCallback : DiffUtil.ItemCallback<QuickSetupListItem>() {

  override fun areItemsTheSame(oldItem: QuickSetupListItem, newItem: QuickSetupListItem) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt &&
      oldItem.season.ids.trakt == newItem.season.ids.trakt &&
      oldItem.isHeader == newItem.isHeader

  override fun areContentsTheSame(oldItem: QuickSetupListItem, newItem: QuickSetupListItem) =
    oldItem.episode == newItem.episode &&
      oldItem.season == newItem.season &&
      oldItem.isHeader == newItem.isHeader &&
      oldItem.isChecked == newItem.isChecked
}
