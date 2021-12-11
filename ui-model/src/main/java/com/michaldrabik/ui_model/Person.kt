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
  val department: Department,
  val bio: String?,
  val bioTranslation: String?,
  val characters: List<String>,
  val jobs: List<Job>,
  val episodesCount: Int,
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

  enum class Department(val slug: String) {
    ACTING("Acting"),
    DIRECTING("Directing"),
    WRITING("Writing"),
    SOUND("Sound"),
    UNKNOWN("-")
  }

  enum class Job(val slug: String) {
    DIRECTOR("Director"),
    WRITER("Writer"),
    STORY("Story"),
    SCREENPLAY("Screenplay"),
    MUSIC("Music"),
    ORIGINAL_MUSIC("Original Music Composer"),
    UNKNOWN("-");

    companion object {
      fun fromSlug(slug: String?) = values().firstOrNull { it.slug == slug } ?: UNKNOWN
    }
  }
}
