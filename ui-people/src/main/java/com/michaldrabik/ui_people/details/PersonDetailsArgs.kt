package com.michaldrabik.ui_people.details

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PersonDetailsArgs(
  val isExpanded: Boolean = false,
  val firstVisibleItemPosition: Int = 0,
  val isUpButtonVisible: Boolean = false
) : Parcelable
