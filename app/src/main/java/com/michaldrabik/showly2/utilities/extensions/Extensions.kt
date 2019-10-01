package com.michaldrabik.showly2.utilities.extensions

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.Fade
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.SafeOnClickListener
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset.UTC

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

fun View.visibleIf(condition: Boolean) =
  if (condition) {
    visible()
  } else {
    gone()
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

fun GridLayoutManager.withSpanSizeLookup(action: (Int) -> Int) {
  spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) = action(position)
  }
}

fun Fragment.getQuantityString(stringResId: Int, count: Long) =
  resources.getQuantityString(stringResId, count.toInt(), count)

fun ViewGroup.showSnackbar(
  message: String,
  actionText: Int = R.string.textOk,
  backgroundRes: Int = R.drawable.bg_snackbar_info,
  length: Int = LENGTH_INDEFINITE,
  action: (() -> Unit)? = null
) {
  Snackbar.make(this, message, length).apply {
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

/**
 * Temporary fix for blinking issue. Remove ASAP.
 * https://github.com/material-components/material-components-android/issues/530?ts=2
 */
fun BottomNavigationView.fixBlinking() {
  val menuView = getChildAt(0) as BottomNavigationMenuView
  with(menuView::class.java.getDeclaredField("set")) {
    isAccessible = true
    val transitionSet = (get(menuView) as AutoTransition).apply {
      for (i in transitionCount downTo 0) {
        val transition = getTransitionAt(i) as? Fade ?: continue
        removeTransition(transition)
      }
    }
    set(menuView, transitionSet)
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