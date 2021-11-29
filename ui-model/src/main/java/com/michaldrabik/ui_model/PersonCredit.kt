package com.michaldrabik.ui_model

data class PersonCredit(
  val show: Show?,
  val movie: Movie?,
  val image: Image
) {

  val year = show?.year ?: movie?.year
}
