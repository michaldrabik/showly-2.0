package com.michaldrabik.showly2.model

enum class Genre(
  val slug: String,
  val displayName: String
) {
  ACTION("action", "Action"),
  ADVENTURE("adventure", "Adventure"),
  ANIMATION("animation", "Animation"),
  ANIME("anime", "Anime"),
  COMEDY("comedy", "Comedy"),
  CRIME("crime", "Crime"),
  DOCUMENTARY("documentary", "Documentary"),
  DRAMA("drama", "Drama"),
  FANTASY("fantasy", "Fantasy"),
  HISTORY("history", "History"),
  HORROR("horror", "Horror"),
  SF("science-fiction", "Science-Fiction"),
  THRILLER("thriller", "Thriller"),
  WAR("war", "War"),
  WESTERN("western", "Western")
}
