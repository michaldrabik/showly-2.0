package com.michaldrabik.ui_base.utilities.extensions

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.CoroutineWorker
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.SafeOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

fun Context.notificationManager() = (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

fun CoroutineWorker.notificationManager() = applicationContext.notificationManager()

fun View.onClick(safe: Boolean = true, action: (View) -> Unit) = setOnClickListener(SafeOnClickListener(safe, action))

fun View.onLongClick(action: (View) -> Unit) = setOnLongClickListener {
  action(it)
  true
}

fun List<View>.onClick(safe: Boolean = true, action: (View) -> Unit) = forEach { it.onClick(safe, action) }

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

fun Fragment.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

@ColorInt
fun Context.colorFromAttr(
  @AttrRes attrColor: Int,
  typedValue: TypedValue = TypedValue(),
  resolveRefs: Boolean = true,
): Int {
  theme.resolveAttribute(attrColor, typedValue, resolveRefs)
  return typedValue.data
}

fun Context.colorStateListFromAttr(
  @AttrRes attrColor: Int,
  typedValue: TypedValue = TypedValue(),
  resolveRefs: Boolean = true,
): ColorStateList =
  ColorStateList.valueOf(colorFromAttr(attrColor, typedValue, resolveRefs))

fun Context.getLocaleStringResource(requestedLocale: Locale?, resourceId: Int): String {
  val result: String
  val config = Configuration(resources.configuration)
  config.setLocale(requestedLocale)
  result = createConfigurationContext(config).getText(resourceId).toString()
  return result
}

fun Context.copyToClipboard(text: String) {
  val clip = ClipData.newPlainText("label", text)
  ContextCompat.getSystemService(this, ClipboardManager::class.java)
    .apply {
      this?.setPrimaryClip(clip)
    }
}

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
  context.theme.resolveAttribute(R.attr.selectableItemBackground, this, true)
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

fun ViewPager.nextPage() {
  val itemsCount = adapter?.count ?: 0
  if (itemsCount == 0) return

  when (currentItem) {
    itemsCount - 1 -> currentItem = 0
    else -> currentItem += 1
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

fun <T> MutableList<T>.replace(newItems: Collection<T>) {
  clear()
  addAll(newItems)
}

fun CoroutineScope.launchDelayed(delayMs: Long, action: suspend () -> Unit): Job {
  return launch {
    delay(delayMs)
    action()
  }
}
