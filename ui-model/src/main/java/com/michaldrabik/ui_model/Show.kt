package com.michaldrabik.ui_model

data class Show(
  val ids: Ids,
  val title: String,
  val year: Int,
  val overview: String,
  val firstAired: String,
  val runtime: Int,
  val airTime: AirTime,
  val certification: String,
  val network: String,
  val country: String,
  val trailer: String,
  val homepage: String,
  val status: ShowStatus,
  val rating: Float,
  val votes: Long,
  val commentCount: Long,
  val genres: List<String>,
  val airedEpisodes: Int,
  val createdAt: Long,
  val updatedAt: Long
) {

  val traktId = ids.trakt.id

  val titleNoThe = title.removePrefix("The").trim()

  val isAnime get() = genres.contains(Genre.ANIME.slug)

  companion object {
    val EMPTY = Show(
      ids = Ids(
        trakt = IdTrakt(id = 0),
        slug = IdSlug(id = ""),
        tvdb = IdTvdb(id = 0),
        imdb = IdImdb(id = ""),
        tmdb = IdTmdb(id = 0),
        tvrage = IdTvRage(id = 0)
      ),
      title = "",
      year = 0,
      overview = "",
      firstAired = "",
      runtime = 0,
      airTime = AirTime(day = "", time = "", timezone = ""),
      certification = "",
      network = "",
      country = "",
      trailer = "",
      homepage = "",
      status = ShowStatus.UNKNOWN,
      rating = 0.0f,
      votes = 0,
      commentCount = 0,
      genres = listOf(),
      airedEpisodes = 0,
      createdAt = 0,
      updatedAt = 0
    )
  }
}
