package com.michaldrabik.showly2.utilities.extensions

import android.view.View
import android.view.WindowInsets
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

/**
 * https://chris.banes.dev/2019/04/12/insets-listeners-to-layouts/
 */
fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    requestApplyInsets()
  } else {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}

/**
 * https://chris.banes.dev/2019/04/12/insets-listeners-to-layouts/
 */
fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialSpacing, InitialSpacing) -> Unit) {
  // Create a snapshot of the view's padding state
  val initialPadding = recordInitialPaddingForView(this)
  val initialMargin = recordInitialMarginForView(this)
  // Set an actual OnApplyWindowInsetsListener which proxies to the given
  // lambda, also passing in the original padding state
  setOnApplyWindowInsetsListener { v, insets ->
    f(v, insets, initialPadding, initialMargin)
    // Always return the insets, so that children can also use them
    insets
  }
  // request some insets
  requestApplyInsetsWhenAttached()
}

data class InitialSpacing(
  val left: Int,
  val top: Int,
  val right: Int,
  val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialSpacing(
  view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun recordInitialMarginForView(view: View) = InitialSpacing(
  view.marginLeft, view.marginTop, view.marginRight, view.marginBottom
)

