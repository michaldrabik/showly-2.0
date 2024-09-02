package com.michaldrabik.showly2.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.databinding.ViewWelcomeLanguageBinding
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_settings.helpers.AppLanguage

class WelcomeLanguageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewWelcomeLanguageBinding.inflate(LayoutInflater.from(context), this)

  var onYesClick: (() -> Unit)? = null
  var onNoClick: (() -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      viewWelcomeLanguageYesButton.onClick { onYesClick?.invoke() }
      viewWelcomeLanguageLeaveButton.onClick { onNoClick?.invoke() }
    }
  }

  @SuppressLint("SetTextI18n")
  fun setLanguage(appLanguage: AppLanguage) {
    // This text will always be English.
    binding.viewWelcomeLanguageMessage.text =
      "It seems like your device\'s language is ${appLanguage.displayNameRaw}.\n" +
      "Would you like to use it in Showly app?"
  }
}
