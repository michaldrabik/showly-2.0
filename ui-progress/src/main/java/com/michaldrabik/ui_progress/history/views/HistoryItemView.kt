package com.michaldrabik.ui_progress.history.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.ViewHistoryItemBinding
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
internal class HistoryItemView : ShowView<HistoryListItem.Episode> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewHistoryItemBinding.inflate(LayoutInflater.from(context), this)

  override val imageView: ImageView = binding.itemImage
  override val placeholderView: ImageView = binding.placeholderImage

  private lateinit var item: HistoryListItem.Episode
  var onDetailsClick: ((HistoryListItem.Episode) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()

    onClick { itemClickListener?.invoke(item) }
    with(binding) {
      detailsButton.expandTouch(100)
      detailsButton.onClick { onDetailsClick?.invoke(item) }
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override fun bind(item: HistoryListItem.Episode) {
    this.item = item
    clear()

    with(binding) {
      itemTitle.text =
        if (item.translations?.show?.title.isNullOrBlank()) {
          item.show.title
        } else {
          item.translations?.show?.title
        }

      itemDate.text =
        item.episode.lastWatchedAt?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }

      val episodeTitle = when {
        item.episode.title.isBlank() -> context.getString(R.string.textTba)
        item.translations?.episode?.title?.isBlank() == false -> item.translations.episode.title
        item.episode.title == "Episode ${item.episode.number}" -> String.format(
          ENGLISH,
          context.getString(R.string.textEpisode),
          item.episode.number,
        )
        else -> item.episode.title
      }

      val isNewSeason = item.episode.number == 1
      if (isNewSeason) {
        itemSubtitle.text = context.getString(R.string.textNewSeason)
        itemDescription.text = String.format(ENGLISH, context.getString(R.string.textSeason), item.episode.season)
      } else {
        itemSubtitle.text = String.format(
          ENGLISH,
          context.getString(R.string.textSeasonEpisode),
          item.episode.season,
          item.episode.number,
        ).plus(
          item.episode.numberAbs?.let { if (it > 0 && item.show.isAnime) " ($it)" else "" } ?: "",
        )
        itemDescription.text = episodeTitle
      }
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
      placeholderImage.gone()
      Glide.with(this@HistoryItemView).clear(itemImage)
    }
  }
}
