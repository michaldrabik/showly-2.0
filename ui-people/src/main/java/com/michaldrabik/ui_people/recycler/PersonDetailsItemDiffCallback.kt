package com.michaldrabik.ui_people.recycler

import androidx.recyclerview.widget.DiffUtil

class PersonDetailsItemDiffCallback : DiffUtil.ItemCallback<PersonDetailsItem>() {

  override fun areItemsTheSame(oldItem: PersonDetailsItem, newItem: PersonDetailsItem) =
    when {
      oldItem is PersonDetailsItem.MainInfo && newItem is PersonDetailsItem.MainInfo -> true
      oldItem is PersonDetailsItem.MainBio && newItem is PersonDetailsItem.MainBio -> true
      oldItem is PersonDetailsItem.CreditsMovieItem && newItem is PersonDetailsItem.CreditsMovieItem -> true
      oldItem is PersonDetailsItem.CreditsShowItem && newItem is PersonDetailsItem.CreditsShowItem -> true
      oldItem is PersonDetailsItem.Loading && newItem is PersonDetailsItem.Loading -> true
      oldItem is PersonDetailsItem.CreditsHeader && newItem is PersonDetailsItem.CreditsHeader -> true
      else -> false
    }

  override fun areContentsTheSame(oldItem: PersonDetailsItem, newItem: PersonDetailsItem) =
    when {
      oldItem is PersonDetailsItem.MainInfo && newItem is PersonDetailsItem.MainInfo -> {
        oldItem.person == newItem.person &&
          oldItem.isLoading == newItem.isLoading
      }
      oldItem is PersonDetailsItem.MainBio && newItem is PersonDetailsItem.MainBio -> {
        oldItem == newItem
      }
      oldItem is PersonDetailsItem.CreditsMovieItem && newItem is PersonDetailsItem.CreditsMovieItem -> {
        oldItem == newItem
      }
      oldItem is PersonDetailsItem.CreditsShowItem && newItem is PersonDetailsItem.CreditsShowItem -> {
        oldItem == newItem
      }
      oldItem is PersonDetailsItem.CreditsHeader && newItem is PersonDetailsItem.CreditsHeader -> {
        oldItem.year == newItem.year
      }
      else -> false
    }
}
