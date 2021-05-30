package com.michaldrabik.ui_base.utilities.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment

fun Context.openWebUrl(url: String): String? {
  val i = Intent(Intent.ACTION_VIEW)
  i.data = Uri.parse(url)
  return try {
    startActivity(i)
    url
  } catch (error: ActivityNotFoundException) {
    null
  }
}

fun Fragment.openWebUrl(url: String) = requireContext().openWebUrl(url)

fun View.openWebUrl(url: String) = context.openWebUrl(url)
