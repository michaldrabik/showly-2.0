package com.michaldrabik.ui_widgets

import android.os.Parcelable
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WidgetSettings(
  val showLabel: Boolean,
  val theme: Int
) : Parcelable {

  companion object {
    fun createInitial() = WidgetSettings(
      showLabel = true,
      theme = MODE_NIGHT_YES
    )
  }
}
