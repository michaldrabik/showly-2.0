package com.michaldrabik.ui_people.recycler

import androidx.recyclerview.widget.DiffUtil

class PersonDetailsItemDiffCallback : DiffUtil.ItemCallback<PersonDetailsItem>() {

  override fun areItemsTheSame(oldItem: PersonDetailsItem, newItem: PersonDetailsItem) =
    when {
      oldItem is PersonDetailsItem.MainInfo && newItem is PersonDetailsItem.MainInfo -> true
      oldItem is PersonDetailsItem.MainBio && newItem is PersonDetailsItem.MainBio -> true
      else -> false
    }

  override fun areContentsTheSame(oldItem: PersonDetailsItem, newItem: PersonDetailsItem) =
    when {
      oldItem is PersonDetailsItem.MainInfo && newItem is PersonDetailsItem.MainInfo -> {
        oldItem.person == newItem.person &&
          oldItem.isLoading == newItem.isLoading
      }
      oldItem is PersonDetailsItem.MainBio && newItem is PersonDetailsItem.MainBio -> {
        oldItem.biography == newItem.biography
      }
      else -> false
    }
}
