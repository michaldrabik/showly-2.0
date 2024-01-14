package com.michaldrabik.ui_base.utilities.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.michaldrabik.ui_model.IdImdb

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

fun Context.openImdbUrl(idImdb: IdImdb): String? {
  val i = Intent(Intent.ACTION_VIEW)
  i.data = Uri.parse("imdb:///title/${idImdb.id}")
  return try {
    startActivity(i)
    i.data?.toString()
  } catch (e: ActivityNotFoundException) {
    // IMDb App not installed. Start in web browser
    openWebUrl("http://www.imdb.com/title/${idImdb.id}")
  }
}

fun Fragment.openWebUrl(url: String) = requireContext().openWebUrl(url)

fun Fragment.openImdbUrl(id: IdImdb) = requireContext().openImdbUrl(id)

fun View.openWebUrl(url: String) = context.openWebUrl(url)
