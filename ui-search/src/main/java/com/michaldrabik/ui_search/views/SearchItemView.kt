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
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_search.R
import com.michaldrabik.ui_search.databinding.ViewShowSearchBinding
import com.michaldrabik.ui_search.recycler.SearchListItem

@SuppressLint("SetTextI18n")
class SearchItemView : ShowView<SearchListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val view = ViewShowSearchBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(view.showSearchRoot) {
      onClick { itemClickListener?.invoke(item) }
      onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  private val spaceNano by lazy { context.dimenToPx(R.dimen.spaceNano).toFloat() }

  override val imageView: ImageView = view.showSearchImage
  override val placeholderView: ImageView = view.showSearchPlaceholder

  private lateinit var item: SearchListItem

  override fun bind(item: SearchListItem) {
    clear()
    this.item = item
    with(view) {
      showSearchTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.title
        else item.translation?.title

      bindDescription(item)

      val year = if (item.year > 0) item.year.toString() else ""
      showSearchNetwork.text =
        if (item.network.isNotBlank()) context.getString(R.string.textNetwork, year, item.network)
        else String.format("%s", year)

      showSearchBadge.visibleIf(item.isFollowed)
      showSearchWatchlistBadge.visibleIf(item.isWatchlist)

      showSearchPlaceholder.setImageResource(if (item.isMovie) R.drawable.ic_film else R.drawable.ic_television)
      showSearchIcon.setImageResource(if (item.isMovie) R.drawable.ic_film else R.drawable.ic_television)
      showSearchNetwork.translationY = if (item.isMovie) 0F else spaceNano
      loadImage(item)
    }
  }

  private fun bindDescription(item: SearchListItem) {
    with(view) {
      var overview = if (item.translation?.overview.isNullOrBlank()) {
        item.overview
      } else {
        item.translation?.overview
      }

      val isShowSpoilerHidden = item.isShow && item.isShowSpoilerHidden && !item.isFollowed
      val isMovieSpoilerHidden = item.isMovie && item.isMovieSpoilerHidden && !item.isFollowed
      if (isShowSpoilerHidden || isMovieSpoilerHidden) {
        overview = spoilerRegex.replace(overview.toString(), SPOILERS_HIDE_SYMBOL)
      }

      showSearchDescription.text = overview
      showSearchDescription.visibleIf(item.overview.isNotBlank())
    }
  }

  private fun clear() {
    with(view) {
      showSearchTitle.text = ""
      showSearchDescription.text = ""
      showSearchNetwork.text = ""
      showSearchPlaceholder.gone()
      showSearchBadge.gone()
      Glide.with(this@SearchItemView).clear(showSearchImage)
    }
  }
}
