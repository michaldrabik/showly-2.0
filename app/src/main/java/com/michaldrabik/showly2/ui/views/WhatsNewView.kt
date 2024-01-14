package com.michaldrabik.showly2.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ScrollView
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.databinding.ViewWhatsNewBinding

@SuppressLint("SetTextI18n")
class WhatsNewView : ScrollView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewWhatsNewBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

    val whatsNew = context.assets.open("release_notes.txt")
      .bufferedReader()
      .use { it.readText() }

    with(binding) {
      viewWhatsNewMessage.text = whatsNew
      viewWhatsNewSubtitle.text = context.getString(R.string.textWhatsNewSubtitle, BuildConfig.VERSION_NAME)
    }
  }
}
