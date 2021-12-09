package com.michaldrabik.ui_people.list.recycler

import androidx.recyclerview.widget.DiffUtil

class PeopleItemDiffCallback : DiffUtil.ItemCallback<PeopleListItem>() {

  override fun areItemsTheSame(oldItem: PeopleListItem, newItem: PeopleListItem) =
    when {
      oldItem is PeopleListItem.HeaderItem && newItem is PeopleListItem.HeaderItem -> true
      oldItem is PeopleListItem.PersonItem && newItem is PeopleListItem.PersonItem -> {
        oldItem.person.ids.tmdb.id == newItem.person.ids.tmdb.id
      }
      else -> false
    }

  override fun areContentsTheSame(oldItem: PeopleListItem, newItem: PeopleListItem) = when {
    oldItem is PeopleListItem.HeaderItem && newItem is PeopleListItem.HeaderItem -> {
      oldItem == newItem
    }
    oldItem is PeopleListItem.PersonItem && newItem is PeopleListItem.PersonItem -> {
      oldItem == newItem
    }
    else -> false
  }
}
