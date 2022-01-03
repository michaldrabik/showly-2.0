package com.michaldrabik.ui_base.utilities.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addDivider(@DrawableRes dividerRes: Int, direction: Int = VERTICAL) {
  addItemDecoration(
    DividerItemDecoration(context, direction).apply {
      setDrawable(ContextCompat.getDrawable(context, dividerRes)!!)
    }
  )
}

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
fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialSpacing, InitialSpacing) -> Unit) {
  // Create a snapshot of the view's padding state
  val initialPadding = recordInitialPaddingForView(this)
  val initialMargin = recordInitialMarginForView(this)
  // Set an actual OnApplyWindowInsetsListener which proxies to the given
  // lambda, also passing in the original padding state
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    f(v, insets, initialPadding, initialMargin)
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

private fun recordInitialMarginForView(view: View): InitialSpacing {
  val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
  return InitialSpacing(
    lp?.leftMargin ?: 0, lp?.topMargin ?: 0, lp?.rightMargin ?: 0, lp?.bottomMargin ?: 0
  )
}
