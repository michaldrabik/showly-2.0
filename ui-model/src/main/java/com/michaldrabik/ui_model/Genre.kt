package com.michaldrabik.ui_model

import androidx.annotation.StringRes

enum class Genre(
  val slug: String,
  @StringRes val displayName: Int
) {
  ACTION("action", R.string.textGenreAction),
  ADVENTURE("adventure", R.string.textGenreAdventure),
  ANIMATION("animation", R.string.textGenreAnimation),
  ANIME("anime", R.string.textGenreAnime),
  COMEDY("comedy", R.string.textGenreComedy),
  CRIME("crime", R.string.textGenreCrime),
  DOCUMENTARY("documentary", R.string.textGenreDocumentary),
  DRAMA("drama", R.string.textGenreDrama),
  FANTASY("fantasy", R.string.textGenreFantasy),
  HISTORY("history", R.string.textGenreHistory),
  HORROR("horror", R.string.textGenreHorror),
  SF("science-fiction", R.string.textGenreScienceFiction),
  THRILLER("thriller", R.string.textGenreThriller),
  WAR("war", R.string.textGenreWar),
  WESTERN("western", R.string.textGenreWestern);

  companion object {
    fun fromSlug(slug: String) = values().find { it.slug.equals(slug, true) }
  }
}
