package com.michaldrabik.ui_search.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_search.R
import com.michaldrabik.ui_search.recycler.SearchListItem
import kotlinx.android.synthetic.main.view_suggestion_search.view.*

@SuppressLint("SetTextI18n")
class SearchSuggestionView : ShowView<SearchListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_suggestion_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    suggestionRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = suggestionImage
  override val placeholderView: ImageView = suggestionPlaceholder

  private lateinit var item: SearchListItem

  override fun bind(item: SearchListItem) {
    clear()
    this.item = item
    if (item.isMovie) suggestionPlaceholder.setImageResource(R.drawable.ic_film)

    val translationTitle = item.translation?.title
    suggestionTitle.text =
      if (translationTitle.isNullOrBlank()) item.title
      else translationTitle

    val translationOverview = item.translation?.overview
    suggestionDescription.text =
      if (translationOverview.isNullOrBlank()) item.overview
      else translationOverview

    suggestionNetwork.text =
      if (item.isShow) {
        if (item.year > 0) context.getString(R.string.textNetwork, item.network, item.year.toString())
        else String.format("%s", item.network)
      } else {
        String.format("%s", item.year)
      }

    suggestionDescription.visibleIf(item.overview.isNotBlank())
    suggestionNetwork.visibleIf(item.network.isNotBlank() || item.year > 0)
    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    suggestionTitle.text = ""
    suggestionDescription.text = ""
    suggestionNetwork.text = ""
    suggestionPlaceholder.setImageResource(R.drawable.ic_television)
    suggestionPlaceholder.gone()
    Glide.with(this).clear(suggestionImage)
  }
}
