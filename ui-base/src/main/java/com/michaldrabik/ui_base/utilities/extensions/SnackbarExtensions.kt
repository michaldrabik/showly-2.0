package com.michaldrabik.ui_base.utilities.extensions

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.michaldrabik.ui_base.R

fun ViewGroup.showSnackbar(
  message: String,
  actionText: Int,
  actionTextColor: Int,
  backgroundRes: Int,
  length: Int,
  action: (() -> Unit)? = null
) {
  Snackbar.make(this, message, length).apply {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.let {
      it.maxLines = 5
    }
    view.setBackgroundResource(backgroundRes)
    setTextColor(actionTextColor)
    setActionTextColor(actionTextColor)
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
  showSnackbar(message, actionText, Color.WHITE, R.drawable.bg_snackbar_error, LENGTH_INDEFINITE, action)
}

fun ViewGroup.showInfoSnackbar(message: String, actionText: Int = R.string.textOk, action: () -> Unit = {}, length: Int = LENGTH_SHORT) {
  showSnackbar(message, actionText, Color.BLACK, R.drawable.bg_snackbar_info, length, action)
}
