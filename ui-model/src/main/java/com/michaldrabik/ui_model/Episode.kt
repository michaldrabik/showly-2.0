package com.michaldrabik.ui_model

import android.os.Parcelable
import com.michaldrabik.common.extensions.nowUtcMillis
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Parcelize
data class Episode(
  val season: Int,
  val number: Int,
  val title: String,
  val ids: Ids,
  val overview: String,
  val rating: Float,
  val votes: Int,
  val commentCount: Int,
  val firstAired: ZonedDateTime?,
  val runtime: Int
) : Parcelable {

  companion object {
    val EMPTY = Episode(-1, -1, "", Ids.EMPTY, "", -1F, -1, -1, null, -1)
  }

  fun hasAired(season: Season) =
    when (firstAired) {
      null -> season.episodes.any { it.number > number && it.firstAired != null }
      else -> nowUtcMillis() >= firstAired.toInstant().toEpochMilli()
    }

  fun toDisplayString() = String.format("S.%02d E.%02d - \'%s\'", season, number, title)

  fun getRatingString(): String {
    val decimalSymbols = DecimalFormatSymbols.getInstance().apply {
      decimalSeparator = '.'
    }
    return DecimalFormat("0.0", decimalSymbols).format(rating)
  }
}
