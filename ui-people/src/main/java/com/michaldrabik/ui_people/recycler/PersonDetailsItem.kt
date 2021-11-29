package com.michaldrabik.ui_people.recycler

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import java.time.format.DateTimeFormatter

sealed class PersonDetailsItem {

  data class MainInfo(
    val person: Person,
    val dateFormat: DateTimeFormatter?,
    val isLoading: Boolean,
  ) : PersonDetailsItem()

  data class MainBio(
    val biography: String?,
    val biographyTranslation: String?,
  ) : PersonDetailsItem()

  data class CreditsShowItem(
    val show: Show,
    val image: Image,
  ) : PersonDetailsItem()

  data class CreditsMovieItem(
    val movie: Movie,
    val image: Image,
  ) : PersonDetailsItem()
}
