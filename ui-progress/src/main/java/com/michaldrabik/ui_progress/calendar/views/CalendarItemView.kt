package com.michaldrabik.ui_progress.calendar.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_progress.databinding.ViewCalendarItemBinding
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CalendarItemView : ShowView<CalendarListItem.Episode> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCalendarItemBinding.inflate(LayoutInflater.from(context), this)

  var detailsClickListener: ((CalendarListItem.Episode) -> Unit)? = null
  var checkClickListener: ((CalendarListItem.Episode) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()

    onClick { itemClickListener?.invoke(item) }
    with(binding) {
      calendarItemInfoButton.expandTouch(100)
      calendarItemInfoButton.onClick { detailsClickListener?.invoke(item) }
      calendarItemCheckButton.onClick { checkClickListener?.invoke(item) }
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: CalendarListItem.Episode

  override val imageView: ImageView = binding.calendarItemImage
  override val placeholderView: ImageView = binding.calendarItemPlaceholder

  override fun bind(item: CalendarListItem.Episode) {
    this.item = item
    clear()

    with(binding) {
      calendarItemTitle.text =
        if (item.translations?.show?.title.isNullOrBlank()) item.show.title
        else item.translations?.show?.title

      calendarItemDateText.text =
        item.episode.firstAired?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }

      val episodeTitle = when {
        item.episode.title.isBlank() -> context.getString(R.string.textTba)
        item.translations?.episode?.title?.isBlank() == false -> item.translations.episode.title
        item.episode.title == "Episode ${item.episode.number}" -> String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode.number)
        else -> item.episode.title
      }

      val isNewSeason = item.episode.number == 1
      if (isNewSeason) {
        val title = String.format(ENGLISH, context.getString(R.string.textSeason), item.episode.season)
        calendarItemSubtitle.text = context.getString(R.string.textNewSeason)
        calendarItemSubtitle2.text =
          if (item.isSpoilerHidden && item.spoilers?.isEpisodeTitleHidden == true) {
            calendarItemSubtitle2.tag = title
            SPOILERS_REGEX.replace(title, SPOILERS_HIDE_SYMBOL)
          } else {
            title
          }
      } else {
        calendarItemSubtitle.text = String.format(
          ENGLISH,
          context.getString(R.string.textSeasonEpisode),
          item.episode.season,
          item.episode.number
        ).plus(
          item.episode.numberAbs?.let { if (it > 0 && item.show.isAnime) " ($it)" else "" } ?: ""
        )
        calendarItemSubtitle2.text =
          if (item.isSpoilerHidden && item.spoilers?.isEpisodeTitleHidden == true) {
            calendarItemSubtitle2.tag = episodeTitle
            SPOILERS_REGEX.replace(episodeTitle, SPOILERS_HIDE_SYMBOL)
          } else {
            episodeTitle
          }
      }

      if (item.isSpoilerHidden && item.spoilers?.isTapToReveal == true) {
        calendarItemSubtitle2.onClick { view ->
          view.tag?.let { calendarItemSubtitle2.text = it.toString() }
          view.isClickable = false
        }
      }

      calendarItemCheckButton.visibleIf(!item.isWatched && !item.isWatchlist)
      calendarItemInfoButton.visibleIf(!item.isWatchlist)
      calendarItemBadge.visibleIf(item.isWatchlist)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translations?.show == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      calendarItemPlaceholder.gone()
      Glide.with(this@CalendarItemView).clear(calendarItemImage)
    }
  }
}
