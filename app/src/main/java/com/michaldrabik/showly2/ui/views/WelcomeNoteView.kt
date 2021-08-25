package com.michaldrabik.showly2.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.utilities.extensions.getLocaleStringResource
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_settings.helpers.AppLanguage
import kotlinx.android.synthetic.main.view_welcome_note.view.*
import java.util.Locale

class WelcomeNoteView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onOkClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_welcome_note, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewWelcomeNoteYesButton.onClick { onOkClickListener?.invoke() }
  }

  fun setLanguage(language: AppLanguage) {
    if (language == AppLanguage.ENGLISH) return
    val locale = Locale(language.code)
    viewWelcomeNoteTitle.text = context.getLocaleStringResource(locale, R.string.textDisclaimerTitle)
    viewWelcomeNoteMessage.text = context.getLocaleStringResource(locale, R.string.textDisclaimerText)
    viewWelcomeNoteYesButton.text = context.getLocaleStringResource(locale, R.string.textDisclaimerConfirmText)
  }
}
