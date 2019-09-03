package com.michaldrabik.showly2.utilities.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.michaldrabik.showly2.R
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset.UTC

fun nowUtc(): OffsetDateTime = OffsetDateTime.now(UTC)

fun View.onClick(action: (View) -> Unit) = setOnClickListener { action(it) }

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun View.visible() {
  if (visibility != VISIBLE) visibility = VISIBLE
}

fun View.gone() {
  if (visibility != GONE) visibility = GONE
}

fun View.visibleIf(condition: Boolean) =
  if (condition) {
    visible()
  } else {
    gone()
  }

fun View.fadeIn(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  visibility = VISIBLE
  alpha = 0F
  animate().alpha(1F).setDuration(duration).setStartDelay(startDelay).withEndAction(endAction).start()
}

fun View.fadeOut(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  animate().alpha(0F).setDuration(duration).setStartDelay(startDelay).withEndAction(endAction).start()
}

fun GridLayoutManager.withSpanSizeLookup(action: (Int) -> Int) {
  spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) = action(position)
  }
}

fun Fragment.getQuantityString(stringResId: Int, count: Long) =
  resources.getQuantityString(stringResId, count.toInt(), count)

fun ViewGroup.showErrorSnackbar(message: String, actionText: Int = R.string.textOk, action: () -> Unit = {}) {
  Snackbar.make(this, message, LENGTH_INDEFINITE).apply {
    view.setBackgroundResource(R.drawable.bg_error_snackbar)
    setActionTextColor(Color.WHITE)
    setAction(actionText) {
      dismiss()
      action()
    }
    show()
  }
}
