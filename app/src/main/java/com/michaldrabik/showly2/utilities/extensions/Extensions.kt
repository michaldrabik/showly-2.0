package com.michaldrabik.showly2.utilities.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.michaldrabik.showly2.Config.DISPLAY_DATE_FORMAT
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.SafeOnClickListener
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.Temporal

fun nowUtc(): OffsetDateTime = OffsetDateTime.now(UTC)

fun nowUtcMillis(): Long = nowUtc().toInstant().toEpochMilli()

fun View.onClick(safe: Boolean = true, action: (View) -> Unit) = setOnClickListener(SafeOnClickListener(safe, action))

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun screenHeight() = Resources.getSystem().displayMetrics.heightPixels

fun View.visible() {
  if (visibility != VISIBLE) visibility = VISIBLE
}

fun View.gone() {
  if (visibility != GONE) visibility = GONE
}

fun View.invisible() {
  if (visibility != INVISIBLE) visibility = INVISIBLE
}

fun View.visibleIf(condition: Boolean, gone: Boolean = true) =
  if (condition) {
    visible()
  } else {
    if (gone) gone() else invisible()
  }

fun View.fadeIf(condition: Boolean) =
  if (condition) {
    fadeIn()
  } else {
    fadeOut()
  }

fun View.fadeIn(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  if (visibility == VISIBLE) return
  visibility = VISIBLE
  alpha = 0F
  animate().alpha(1F).setDuration(duration).setStartDelay(startDelay).withEndAction(endAction).start()
}

fun View.fadeOut(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  if (visibility == GONE) return
  animate().alpha(0F).setDuration(duration).setStartDelay(startDelay).withEndAction {
    gone()
    endAction()
  }.start()
}

fun View.shake() = ObjectAnimator.ofFloat(this, "translationX", 0F, -15F, 15F, -10F, 10F, -5F, 5F, 0F)
  .setDuration(500)
  .start()

fun View.bump(action: () -> Unit = {}) {
  val x = ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.1F, 1F)
  val y = ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.1F, 1F)

  AnimatorSet().apply {
    playTogether(x, y)
    duration = 250
    doOnEnd { action() }
    start()
  }
}

fun GridLayoutManager.withSpanSizeLookup(action: (Int) -> Int) {
  spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) = action(position)
  }
}

fun ViewGroup.showSnackbar(
  message: String,
  actionText: Int = R.string.textOk,
  backgroundRes: Int = R.drawable.bg_snackbar_info,
  length: Int = LENGTH_INDEFINITE,
  action: (() -> Unit)? = null
) {
  Snackbar.make(this, message, length).apply {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.let {
      it.maxLines = 5
    }
    view.setBackgroundResource(backgroundRes)
    setActionTextColor(Color.WHITE)
    if (action != null) {
      setAction(actionText) {
        dismiss()
        action()
      }
    }
    show()
  }
}

fun ViewGroup.showErrorSnackbar(message: String, actionText: Int = R.string.textOk, action: () -> Unit = {}) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_error, LENGTH_INDEFINITE, action)
}

fun ViewGroup.showInfoSnackbar(message: String, actionText: Int = R.string.textOk) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_info, LENGTH_LONG)
}

fun ViewGroup.showShortInfoSnackbar(message: String, actionText: Int = R.string.textOk) {
  showSnackbar(message, actionText, R.drawable.bg_snackbar_info, LENGTH_SHORT)
}

fun View.showKeyboard() {
  val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  requestFocus()
  inputMethodManager.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
  (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
    hideSoftInputFromWindow(windowToken, 0)
  }
}

fun View.addRipple() = with(TypedValue()) {
  context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
  setBackgroundResource(resourceId)
}

fun View.expandTouchArea(amount: Int = 50) {
  val rect = Rect()
  this.getHitRect(rect)
  rect.top -= amount
  rect.right += amount
  rect.bottom += amount
  rect.left -= amount
  (this.parent as View).touchDelegate = TouchDelegate(rect, this)
}

fun CheckBox.setCheckedSilent(isChecked: Boolean, action: (View, Boolean) -> Unit = { _, _ -> }) {
  setOnCheckedChangeListener { _, _ -> }
  setChecked(isChecked)
  setOnCheckedChangeListener(action)
}

fun ZonedDateTime.toLocalTimeZone(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

fun Temporal.toDisplayString(): String = DISPLAY_DATE_FORMAT.format(this)

fun ProgressBar.setAnimatedProgress(value: Int) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    setProgress(value, true)
  } else {
    progress = value
  }
}