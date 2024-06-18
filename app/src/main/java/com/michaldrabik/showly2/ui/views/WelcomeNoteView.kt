package com.michaldrabik.showly2.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.databinding.ViewWelcomeNoteBinding
import com.michaldrabik.ui_base.utilities.extensions.getLocaleStringResource
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.locale.AppLanguage
import java.util.Locale

class WelcomeNoteView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewWelcomeNoteBinding.inflate(LayoutInflater.from(context), this)

  var onOkClickListener: (() -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.viewWelcomeNoteYesButton.onClick { onOkClickListener?.invoke() }
  }

  fun setLanguage(language: AppLanguage) {
    if (language == AppLanguage.ENGLISH) return
    val locale = Locale(language.code)
    with(binding) {
      viewWelcomeNoteTitle.text = context.getLocaleStringResource(locale, R.string.textDisclaimerTitle)
      viewWelcomeNoteMessage.text = context.getLocaleStringResource(locale, R.string.textDisclaimerText)
      viewWelcomeNoteYesButton.text = context.getLocaleStringResource(locale, R.string.textDisclaimerConfirmText)
    }
  }
}
