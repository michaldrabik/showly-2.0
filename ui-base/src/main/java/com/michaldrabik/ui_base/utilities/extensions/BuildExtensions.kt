package com.michaldrabik.ui_base.utilities.extensions

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
inline fun withApiAtLeast(value: Int, action: () -> Unit) {
  if (Build.VERSION.SDK_INT >= value) {
    action()
  }
}
