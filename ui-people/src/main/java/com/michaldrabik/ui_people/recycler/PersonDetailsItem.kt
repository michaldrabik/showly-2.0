package com.michaldrabik.ui_people.recycler

import com.michaldrabik.ui_model.Person
import java.time.format.DateTimeFormatter

sealed class PersonDetailsItem() {

  data class MainInfo(
    val person: Person,
    val dateFormat: DateTimeFormatter?,
    val isLoading: Boolean,
  ) : PersonDetailsItem()

  data class MainBio(
    val biography: String?,
    val biographyTranslation: String?,
  ) : PersonDetailsItem()
}
