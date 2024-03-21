package com.michaldrabik.ui_progress.history.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.ViewHistoryHeaderBinding
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class HistoryHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewHistoryHeaderBinding.inflate(LayoutInflater.from(context), this)

  private val now = nowUtc().toLocalZone()
  private lateinit var dateFormat: DateTimeFormatter

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    updatePadding(
      top = context.dimenToPx(R.dimen.spaceBig),
      bottom = context.dimenToPx(R.dimen.spaceTiny),
      left = context.dimenToPx(R.dimen.screenMarginHorizontal),
      right = context.dimenToPx(R.dimen.screenMarginHorizontal)
    )
  }

  private fun initDateFormat(language: String) {
    if (!::dateFormat.isInitialized) {
      dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale(language))
    }
  }

  fun bind(item: HistoryListItem.Header, position: Int) {
    with(binding) {
      text.text = if (now.dayOfYear == item.date.dayOfYear) {
        context.getString(R.string.textToday)
      } else {
        initDateFormat(item.language)
        item.date.format(dateFormat).capitalizeWords()
      }
      updatePadding(
        top = context.dimenToPx(if (position == 1) R.dimen.spaceMedium else R.dimen.spaceBig)
      )
    }
  }
}
