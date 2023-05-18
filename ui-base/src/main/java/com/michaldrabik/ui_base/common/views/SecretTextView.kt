package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class SecretTextView : AppCompatTextView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var isSwitchable = true
    set(value) {
      field = value
      if (value) {
        setOnClickListener { toggle() }
      } else {
        setOnClickListener(null)
      }
    }

  private var originalText: String? = null
  private var isSecret = true

  init {
    isSwitchable = true
  }

  fun setSecretText(text: String?, isSecret: Boolean = true) {
    this.isSecret = isSecret
    this.originalText = text
    if (isSecret) {
      this.text = originalText?.map { '*' }?.joinToString("")
      ellipsize = null
    } else {
      this.text = originalText
      ellipsize = TextUtils.TruncateAt.END
    }
  }

  private fun toggle() {
    if (!isSwitchable) return
    text = if (isSecret) {
      ellipsize = TextUtils.TruncateAt.END
      originalText
    } else {
      ellipsize = null
      originalText?.map { '*' }?.joinToString("")
    }
    isSecret = !isSecret
  }
}
