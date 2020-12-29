package com.michaldrabik.ui_widgets

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WidgetSettings(
  val showLabel: Boolean
) : Parcelable {

  companion object {
    fun createInitial() = WidgetSettings(
      showLabel = true
    )
  }
}
