package com.michaldrabik.showly2.utilities

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun Fragment.dimenToPx(@DimenRes dimenResId: Int) = requireContext().dimenToPx(dimenResId)

fun Context.screenWidth() = Resources.getSystem().displayMetrics.widthPixels
