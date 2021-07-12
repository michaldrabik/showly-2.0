package com.michaldrabik.ui_base.utilities.extensions

import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.michaldrabik.ui_base.R

fun ViewGroup.showSnackbar(
  message: String,
  actionText: Int,
  textColor: Int,
  backgroundRes: Int,
  length: Int,
  action: (() -> Unit)? = null,
): Snackbar {
  return Snackbar.make(this, message, length).apply {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.let {
      it.maxLines = 5
    }
    view.setBackgroundResource(backgroundRes)
    setTextColor(textColor)
    setActionTextColor(textColor)
    if (action != null) {
      setAction(actionText) {
        dismiss()
        action()
      }
    }
    show()
  }
}

fun ViewGroup.showInfoSnackbar(
  message: String,
  actionText: Int = R.string.textOk,
  length: Int = LENGTH_SHORT,
  action: (() -> Unit)? = null,
): Snackbar {
  val textColor = context.colorFromAttr(R.attr.textColorInfoSnackbar)
  return showSnackbar(message, actionText, textColor, R.drawable.bg_snackbar_info, length, action)
}

fun ViewGroup.showErrorSnackbar(
  message: String,
  actionText: Int = R.string.textOk,
  action: () -> Unit = {},
): Snackbar {
  val textColor = context.colorFromAttr(R.attr.textColorErrorSnackbar)
  return showSnackbar(message, actionText, textColor, R.drawable.bg_snackbar_error, LENGTH_INDEFINITE, action)
}
