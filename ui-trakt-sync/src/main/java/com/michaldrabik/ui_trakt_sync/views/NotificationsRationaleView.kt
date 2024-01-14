package com.michaldrabik.ui_trakt_sync.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.michaldrabik.ui_trakt_sync.databinding.ViewTraktNotificationsRationaleBinding

@SuppressLint("SetTextI18n")
class NotificationsRationaleView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewTraktNotificationsRationaleBinding.inflate(LayoutInflater.from(context), this)
}
