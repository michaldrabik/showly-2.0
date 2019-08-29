package com.michaldrabik.showly2.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun Fragment.dimenToPx(@DimenRes dimenResId: Int) = requireContext().dimenToPx(dimenResId)

fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun View.visible() {
  visibility = VISIBLE
}

fun View.gone() {
  visibility = GONE
}

fun View.visibleIf(condition: Boolean) =
  if (condition) {
    visible()
  } else {
    gone()
  }

fun GridLayoutManager.withSpanSizeLookup(action: (Int) -> Int) {
  spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) = action(position)
  }
}

inline fun RequestBuilder<Drawable>.withFailListener(crossinline action: () -> Unit) =
  addListener(object : RequestListener<Drawable?> {
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
      action()
      return false
    }

    override fun onResourceReady(
      resource: Drawable?,
      model: Any?,
      target: Target<Drawable?>?,
      dataSource: DataSource?,
      isFirstResource: Boolean
    ) = false
  })

inline fun RequestBuilder<Drawable>.withSuccessListener(crossinline action: () -> Unit) =
  addListener(object : RequestListener<Drawable?> {
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean) = false

    override fun onResourceReady(
      resource: Drawable?,
      model: Any?,
      target: Target<Drawable?>?,
      dataSource: DataSource?,
      isFirstResource: Boolean
    ): Boolean {
      action()
      return false
    }
  })
