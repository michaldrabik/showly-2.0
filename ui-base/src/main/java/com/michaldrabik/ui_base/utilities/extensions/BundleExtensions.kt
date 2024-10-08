package com.michaldrabik.ui_base.utilities.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

fun Fragment.requireString(
  key: String?,
  default: String? = null,
) = requireArguments().getString(key, default)!!

fun Fragment.requireStringArray(key: String?) = requireArguments().getStringArrayList(key)!!

fun Fragment.requireLong(key: String?) = requireArguments().getLong(key)

fun Fragment.requireLongArray(key: String?) = requireArguments().getLongArray(key)!!

fun Fragment.requireBoolean(key: String?) = requireArguments().getBoolean(key)

fun <T> Fragment.requireSerializable(key: String?) = requireArguments().getSerializable(key) as T

fun <T : Parcelable> Fragment.requireParcelable(key: String?) = optionalParcelable<T>(key)!!

fun <T : Parcelable> Fragment.optionalParcelable(key: String?) = requireArguments().getParcelable<T>(key)

inline fun <reified T : Parcelable> Bundle.requireParcelable(key: String): T =
  when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)!!
    else -> @Suppress("DEPRECATION") (getParcelable(key) as? T)!!
  }

inline fun <reified T : Parcelable> Bundle.optionalParcelable(key: String): T? =
  when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") (getParcelable(key) as? T)
  }

inline fun <reified T : Serializable> Bundle.requireSerializable(key: String): T =
  when {
    Build.VERSION.SDK_INT >= 33 -> getSerializable(key, T::class.java)!!
    else -> @Suppress("DEPRECATION") (getSerializable(key) as? T)!!
  }
