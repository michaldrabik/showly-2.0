package com.michaldrabik.ui_model

import android.os.Parcelable
import com.michaldrabik.common.extensions.nowUtcDay
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.Period

@Parcelize
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
) : Parcelable {

  fun getAge() = when {
    birthday != null && deathday != null -> Period.between(birthday, deathday).years
    birthday != null -> Period.between(birthday, nowUtcDay()).years
    else -> null
  }

  enum class Type(val slug: String) {
    ACTING("Acting"),
    DIRECTING("Directing"),
    WRITING("Writing"),
    SOUND("Sound"),
    UNKNOWN("")
  }
}
