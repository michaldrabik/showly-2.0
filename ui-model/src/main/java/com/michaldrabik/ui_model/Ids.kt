@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.ui_model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ids(
  val trakt: IdTrakt,
  val slug: IdSlug,
  val tvdb: IdTvdb,
  val imdb: IdImdb,
  val tmdb: IdTmdb,
  val tvrage: IdTvRage,
) : Parcelable {

  companion object {
    val EMPTY = Ids(
      IdTrakt(),
      IdSlug(),
      IdTvdb(),
      IdImdb(),
      IdTmdb(),
      IdTvRage()
    )
  }
}

@JvmInline
value class IdTrakt(val id: Long = -1)

@JvmInline
value class IdTvdb(val id: Long = -1)

@JvmInline
value class IdImdb(val id: String = "")

@JvmInline
value class IdTmdb(val id: Long = -1)

@JvmInline
value class IdTvRage(val id: Long = -1)

@JvmInline
value class IdSlug(val id: String = "")
