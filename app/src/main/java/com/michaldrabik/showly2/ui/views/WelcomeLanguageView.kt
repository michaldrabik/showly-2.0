package com.michaldrabik.showly2.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_settings.helpers.AppLanguage
import kotlinx.android.synthetic.main.view_welcome_language.view.*

class WelcomeLanguageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onYesClick: (() -> Unit)? = null
  var onNoClick: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_welcome_language, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewWelcomeLanguageYesButton.onClick { onYesClick?.invoke() }
    viewWelcomeLanguageLeaveButton.onClick { onNoClick?.invoke() }
  }

  @SuppressLint("SetTextI18n")
  fun setLanguage(appLanguage: AppLanguage) {
    // This text will always be English.
    viewWelcomeLanguageMessage.text = "It seems like your device\'s language is ${appLanguage.displayNameRaw}.\n" +
      "Would you like to use it in Showly app?"
  }
}
