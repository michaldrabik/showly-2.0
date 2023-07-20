package com.michaldrabik.ui_model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
  val username: String,
  val avatarUrl: String,
) : Parcelable
