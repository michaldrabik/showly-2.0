package com.michaldrabik.ui_people

import com.michaldrabik.ui_model.Person
import java.time.format.DateTimeFormatter

data class PersonDetailsUiState(
  val isLoading: Boolean? = null,
  val personDetails: Person? = null,
  val dateFormat: DateTimeFormatter? = null,
)
