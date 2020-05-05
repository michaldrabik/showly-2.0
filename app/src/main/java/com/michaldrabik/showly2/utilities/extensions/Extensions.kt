package com.michaldrabik.showly2.utilities.extensions

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.ProgressBar
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.michaldrabik.showly2.utilities.SafeOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun View.onClick(safe: Boolean = true, action: (View) -> Unit) = setOnClickListener(SafeOnClickListener(safe, action))

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun Context.notificationManager() = (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun screenHeight() = Resources.getSystem().displayMetrics.heightPixels

fun GridLayoutManager.withSpanSizeLookup(action: (Int) -> Int) {
  spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int) = action(position)
  }
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

fun View.expandTouch(amount: Int = 50) {
  val rect = Rect()
  this.getHitRect(rect)
  rect.top -= amount
  rect.right += amount
  rect.bottom += amount
  rect.left -= amount
  (this.parent as View).touchDelegate = TouchDelegate(rect, this)
}

fun CompoundButton.setCheckedSilent(isChecked: Boolean, action: (View, Boolean) -> Unit = { _, _ -> }) {
  setOnCheckedChangeListener { _, _ -> }
  setChecked(isChecked)
  setOnCheckedChangeListener(action)
}

fun ProgressBar.setAnimatedProgress(value: Int) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    setProgress(value, true)
  } else {
    progress = value
  }
}

fun ViewPager2.nextPage() {
  val itemsCount = adapter?.itemCount ?: 0
  if (itemsCount == 0) return

  when (currentItem) {
    itemsCount - 1 -> currentItem = 0
    else -> currentItem += 1
  }
}

fun <T> MutableList<T>.replaceItem(oldItem: T, newItem: T) {
  val index = indexOf(oldItem)
  removeAt(index)
  add(index, newItem)
}

inline fun <T> MutableList<T>.findReplace(newItem: T, predicate: (T) -> Boolean) {
  find(predicate)?.let { replaceItem(it, newItem) }
}

fun <T> MutableList<T>.replace(newItems: List<T>) {
  clear()
  addAll(newItems)
}

fun CoroutineScope.launchDelayed(delayMs: Long, action: () -> Unit): Job {
  return launch {
    delay(delayMs)
    action()
  }
}
