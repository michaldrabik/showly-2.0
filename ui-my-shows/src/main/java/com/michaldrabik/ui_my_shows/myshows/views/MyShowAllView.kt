package com.michaldrabik.ui_my_shows.myshows.views

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
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_show_all.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyShowAllView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_show_all, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    myShowAllRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = myShowAllImage
  override val placeholderView: ImageView = myShowAllPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item
    myShowAllProgress.visibleIf(item.isLoading)
    myShowAllTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title

    myShowAllDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    myShowAllNetwork.text =
      if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
      else String.format("%s", item.show.network)

    myShowAllRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
    myShowAllDescription.visibleIf(item.show.overview.isNotBlank())
    myShowAllNetwork.visibleIf(item.show.network.isNotBlank())

    item.userRating?.let {
      myShowAllUserStarIcon.visible()
      myShowAllUserRating.visible()
      myShowAllUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    myShowAllTitle.text = ""
    myShowAllDescription.text = ""
    myShowAllNetwork.text = ""
    myShowAllRating.text = ""
    myShowAllPlaceholder.gone()
    myShowAllUserStarIcon.gone()
    myShowAllUserRating.gone()
    Glide.with(this).clear(myShowAllImage)
  }
}
