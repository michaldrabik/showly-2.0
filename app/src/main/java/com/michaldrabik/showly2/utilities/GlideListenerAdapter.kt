package com.michaldrabik.showly2.utilities

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

abstract class GlideListenerAdapter(
  private val readyAction: () -> Unit = {},
  private val failAction: () -> Unit = {}
) : RequestListener<Drawable> {
  override fun onLoadFailed(
    e: GlideException?,
    model: Any?,
    target: Target<Drawable>?,
    isFirstResource: Boolean
  ): Boolean {
    readyAction()
    return false
  }

  override fun onResourceReady(
    resource: Drawable?,
    model: Any?,
    target: Target<Drawable>?,
    dataSource: DataSource?,
    isFirstResource: Boolean
  ): Boolean {
    failAction()
    return false
  }
}