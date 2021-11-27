package com.michaldrabik.ui_model

import java.time.LocalDate

data class Person(
  val ids: Ids,
  val name: String,
  val type: Type,
  val bio: String?,
  val character: String?,
  val birthplace: String?,
  val imagePath: String?,
  val homepage: String?,
  val birthday: LocalDate?,
  val deathday: LocalDate?,
) {

  enum class Type(val slug: String) {
    ACTING("Acting"),
    DIRECTING("Directing"),
    WRITING("Writing"),
    SOUND("Sound"),
    UNKNOWN("")
  }
}
