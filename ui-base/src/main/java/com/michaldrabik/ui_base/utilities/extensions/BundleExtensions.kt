package com.michaldrabik.ui_base.utilities.extensions

import android.os.Parcelable
import androidx.fragment.app.Fragment

fun Fragment.requireString(key: String?, default: String? = null) =
  requireArguments().getString(key, default)!!

fun Fragment.requireStringArray(key: String?) =
  requireArguments().getStringArrayList(key)!!

fun Fragment.requireLong(key: String?) =
  requireArguments().getLong(key)

fun Fragment.requireLongArray(key: String?) =
  requireArguments().getLongArray(key)!!

fun Fragment.requireBoolean(key: String?) =
  requireArguments().getBoolean(key)

@Suppress("UNCHECKED_CAST")
fun <T> Fragment.requireSerializable(key: String?) =
  requireArguments().getSerializable(key) as T

fun <T : Parcelable> Fragment.requireParcelable(key: String?) =
  optionalParcelable<T>(key)!!

fun <T : Parcelable> Fragment.optionalParcelable(key: String?) =
  requireArguments().getParcelable<T>(key)

fun Fragment.optionalIntArray(key: String?) =
  requireArguments().getIntArray(key)
