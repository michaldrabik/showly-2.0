package com.michaldrabik.ui_settings.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.michaldrabik.common.Config

object PlayStoreHelper {

  fun openPlayStorePage(activity: Activity) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse(Config.PLAYSTORE_URL)
      setPackage("com.android.vending")
    }
    if (intent.resolveActivity(activity.packageManager) != null) {
      activity.startActivity(intent)
    }
  }
}
