package com.michaldrabik.showly2.utilities.extensions

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.michaldrabik.showly2.R

fun ViewGroup.showSnackbar(
  message: String,
  actionText: Int = R.string.textOk,
  backgroundRes: Int = R.drawable.bg_snackbar_info,
  length: Int = LENGTH_INDEFINITE,
  action: (() -> Unit)? = null
) {
  Snackbar.make(this, message, length).apply {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.let {
      it.maxLines = 5
    }
    view.setBackgroundResource(backgroundRes)
    setTextColor(Color.WHITE)
    setActionTextColor(Color.WHITE)
    if (action != null) {
      setAction(actionText) {
        dismiss()
        action()
      }
    }
    show()
  }
}

fun ViewGroup.showErrorSnackbar(message: String, actionText: Int = R.string.textOk, action: () -> Unit = {}) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_error, LENGTH_INDEFINITE, action)
}

fun ViewGroup.showInfoSnackbar(message: String, actionText: Int = R.string.textOk) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_info, LENGTH_LONG)
}

fun ViewGroup.showShortInfoSnackbar(message: String, actionText: Int = R.string.textOk) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_info, LENGTH_SHORT)
}
