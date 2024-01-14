package com.michaldrabik.data_remote.tmdb.model

data class TmdbStreamingCountry(
  val link: String,
  val flatrate: List<TmdbStreamingService>?,
  val free: List<TmdbStreamingService>?,
  val buy: List<TmdbStreamingService>?,
  val rent: List<TmdbStreamingService>?,
  val ads: List<TmdbStreamingService>?,
)
