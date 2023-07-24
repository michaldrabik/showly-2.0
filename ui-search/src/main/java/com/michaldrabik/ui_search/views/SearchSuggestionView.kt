package com.michaldrabik.ui_search.views

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
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_search.R
import com.michaldrabik.ui_search.databinding.ViewSuggestionSearchBinding
import com.michaldrabik.ui_search.recycler.SearchListItem

@SuppressLint("SetTextI18n")
class SearchSuggestionView : ShowView<SearchListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val view = ViewSuggestionSearchBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    view.suggestionRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = view.suggestionImage
  override val placeholderView: ImageView = view.suggestionPlaceholder

  private lateinit var item: SearchListItem

  override fun bind(item: SearchListItem) {
    clear()
    this.item = item
    with(view) {
      if (item.isMovie) suggestionPlaceholder.setImageResource(R.drawable.ic_film)

      val translationTitle = item.translation?.title
      suggestionTitle.text =
        if (translationTitle.isNullOrBlank()) item.title
        else translationTitle

      bindDescription(item)

      suggestionNetwork.text =
        if (item.isShow) {
          if (item.year > 0) context.getString(R.string.textNetwork, item.network, item.year.toString())
          else String.format("%s", item.network)
        } else {
          String.format("%s", item.year)
        }

      suggestionBadge.visibleIf(item.isFollowed)
      suggestionWatchlistBadge.visibleIf(item.isWatchlist)
      suggestionDescription.visibleIf(item.overview.isNotBlank())
      suggestionNetwork.visibleIf(item.network.isNotBlank() || item.year > 0)

      loadImage(item)
    }
  }

  private fun bindDescription(item: SearchListItem) {
    with(view) {
      var isSpoilerHidden = false
      var overview = if (item.translation?.overview.isNullOrBlank()) {
        item.overview
      } else {
        item.translation?.overview
      }

      if (item.isShow) {
        val isMyHidden = item.spoilers.isMyShowsHidden && item.isFollowed
        val isWatchlistHidden = item.spoilers.isWatchlistShowsHidden && item.isWatchlist
        val isNotCollectedHidden = item.spoilers.isNotCollectedShowsHidden && (!item.isFollowed && !item.isWatchlist)
        if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
          suggestionDescription.tag = overview.toString()
          overview = SPOILERS_REGEX.replace(overview.toString(), SPOILERS_HIDE_SYMBOL)
          isSpoilerHidden = true
        }
      }

      if (item.isMovie) {
        val isMyHidden = item.spoilers.isMyMoviesHidden && item.isFollowed
        val isWatchlistHidden = item.spoilers.isWatchlistMoviesHidden && item.isWatchlist
        val isNotCollectedHidden = item.spoilers.isNotCollectedMoviesHidden && (!item.isFollowed && !item.isWatchlist)
        if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
          suggestionDescription.tag = overview.toString()
          overview = SPOILERS_REGEX.replace(overview.toString(), SPOILERS_HIDE_SYMBOL)
          isSpoilerHidden = true
        }
      }

      with(suggestionDescription) {
        text = overview
        visibleIf(item.overview.isNotBlank())
        if (isSpoilerHidden && item.spoilers.isTapToReveal) {
          onClick { view ->
            view.tag?.let { text = it.toString() }
            view.isClickable = false
          }
        }
      }
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(view) {
      suggestionTitle.text = ""
      suggestionDescription.text = ""
      suggestionNetwork.text = ""
      suggestionPlaceholder.setImageResource(R.drawable.ic_television)
      suggestionPlaceholder.gone()
      Glide.with(this@SearchSuggestionView).clear(suggestionImage)
    }
  }
}
