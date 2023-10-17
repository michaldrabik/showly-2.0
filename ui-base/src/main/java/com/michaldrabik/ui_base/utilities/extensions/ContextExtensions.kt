package com.michaldrabik.ui_base.utilities.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.R
import java.util.Locale

fun Context.isTablet() = resources.getBoolean(R.bool.isTablet)

fun Context.notificationManager() = NotificationManagerCompat.from(this)

fun Context.dimenToPx(@DimenRes dimenResId: Int) = resources.getDimensionPixelSize(dimenResId)

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
