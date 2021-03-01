package com.michaldrabik.ui_progress

import androidx.recyclerview.widget.DiffUtil

class ProgressItemDiffCallback : DiffUtil.ItemCallback<ProgressItem>() {

  override fun areItemsTheSame(oldItem: ProgressItem, newItem: ProgressItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt && oldItem.isHeader() == newItem.isHeader()

  override fun areContentsTheSame(oldItem: ProgressItem, newItem: ProgressItem) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt &&
      oldItem.upcomingEpisode.ids.trakt == newItem.upcomingEpisode.ids.trakt &&
      oldItem.episodesCount == newItem.episodesCount &&
      oldItem.watchedEpisodesCount == newItem.watchedEpisodesCount &&
      oldItem.season == newItem.season &&
      oldItem.season.episodes == newItem.season.episodes &&
      oldItem.upcomingSeason == newItem.upcomingSeason &&
      oldItem.upcomingSeason.episodes == newItem.upcomingSeason.episodes &&
      oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image &&
      oldItem.showTranslation == newItem.showTranslation &&
      oldItem.episodeTranslation == newItem.episodeTranslation &&
      oldItem.headerTextResId == newItem.headerTextResId
}
