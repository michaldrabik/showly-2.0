package com.michaldrabik.ui_progress.calendar.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import kotlinx.android.synthetic.main.view_progress_calendar_item.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class ProgressCalendarItemView : ShowView<ProgressItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var detailsClickListener: ((ProgressItem) -> Unit)? = null

  init {
    inflate(context, R.layout.view_progress_calendar_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()

    onClick { itemClickListener?.invoke(item) }

    progressCalendarItemInfoButton.expandTouch(100)
    progressCalendarItemInfoButton.onClick { detailsClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: ProgressItem

  override val imageView: ImageView = progressCalendarItemImage
  override val placeholderView: ImageView = progressCalendarItemPlaceholder

  override fun bind(item: ProgressItem) {
    this.item = item
    clear()

    progressCalendarItemTitle.text =
      if (item.showTranslation?.title.isNullOrBlank()) item.show.title
      else item.showTranslation?.title

    progressCalendarItemDateText.text =
      item.upcomingEpisode.firstAired?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }

    val isNewSeason = item.upcomingEpisode.number == 1
    if (isNewSeason) {
      progressCalendarItemSubtitle2.text = String.format(ENGLISH, context.getString(R.string.textSeason), item.upcomingEpisode.season)
      progressCalendarItemSubtitle.text = context.getString(R.string.textNewSeason)
    } else {
      val episodeTitle = when {
        item.upcomingEpisode.title.isBlank() -> context.getString(R.string.textTba)
        item.upcomingEpisodeTranslation?.title?.isBlank() == false -> item.upcomingEpisodeTranslation.title
        else -> item.upcomingEpisode.title
      }
      progressCalendarItemSubtitle2.text = episodeTitle
      progressCalendarItemSubtitle.text = String.format(
        ENGLISH,
        context.getString(R.string.textSeasonEpisode),
        item.upcomingEpisode.season,
        item.upcomingEpisode.number
      )
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.showTranslation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    progressCalendarItemTitle.text = ""
    progressCalendarItemSubtitle.text = ""
    progressCalendarItemSubtitle2.text = ""
    progressCalendarItemDateText.text = ""
    progressCalendarItemPlaceholder.gone()
    Glide.with(this).clear(progressCalendarItemImage)
  }
}
