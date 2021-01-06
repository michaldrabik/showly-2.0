package com.michaldrabik.ui_base.utilities.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

fun Context.openWebUrl(url: String) {
  val i = Intent(Intent.ACTION_VIEW)
  i.data = Uri.parse(url)
  startActivity(i)
}

fun Fragment.openWebUrl(url: String) = requireContext().openWebUrl(url)
