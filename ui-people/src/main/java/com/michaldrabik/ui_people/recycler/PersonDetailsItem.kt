package com.michaldrabik.ui_people.recycler

import com.michaldrabik.common.Mode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import java.time.format.DateTimeFormatter

sealed class PersonDetailsItem {

  open fun getId(): Long? = null

  fun isCreditsItem() = this is CreditsHeader || this is CreditsMovieItem || this is CreditsShowItem

  data class MainInfo(
    val person: Person,
    val dateFormat: DateTimeFormatter?,
    val isLoading: Boolean = false
  ) : PersonDetailsItem()

  data class MainBio(
    val biography: String?,
    val biographyTranslation: String?,
  ) : PersonDetailsItem()

  data class CreditsHeader(
    val year: Int?,
  ) : PersonDetailsItem() {
    override fun getId() = year?.toLong()
  }

  data class CreditsShowItem(
    val show: Show,
    val image: Image,
    val isLoading: Boolean = false
  ) : PersonDetailsItem() {
    override fun getId() = show.traktId
  }

  data class CreditsMovieItem(
    val movie: Movie,
    val image: Image,
    val isLoading: Boolean = false
  ) : PersonDetailsItem() {
    override fun getId() = movie.traktId
  }

  data class CreditsFiltersItem(
    val filters: List<Mode>
  ) : PersonDetailsItem()

  object CreditsLoadingItem : PersonDetailsItem()
}
