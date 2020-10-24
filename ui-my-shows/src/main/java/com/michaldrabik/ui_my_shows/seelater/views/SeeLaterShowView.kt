package com.michaldrabik.ui_my_shows.seelater.views

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
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.seelater.recycler.SeeLaterListItem
import kotlinx.android.synthetic.main.view_see_later_show.view.*
import java.util.Locale.ROOT

@SuppressLint("SetTextI18n")
class SeeLaterShowView : ShowView<SeeLaterListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_see_later_show, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    seeLaterShowRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = seeLaterShowImage
  override val placeholderView: ImageView = seeLaterShowPlaceholder

  private lateinit var item: SeeLaterListItem

  override fun bind(
    item: SeeLaterListItem,
    missingImageListener: (SeeLaterListItem, Boolean) -> Unit
  ) {
    clear()
    this.item = item
    seeLaterShowProgress.visibleIf(item.isLoading)
    seeLaterShowTitle.text = item.show.title
    seeLaterShowDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    val year = if (item.show.year > 0) " (${item.show.year})" else ""
    seeLaterShowNetwork.text = "${item.show.network}$year"
    seeLaterShowRating.text = String.format(ROOT, "%.1f", item.show.rating)

    seeLaterShowDescription.visibleIf(item.show.overview.isNotBlank())
    seeLaterShowNetwork.visibleIf(item.show.network.isNotBlank())

    loadImage(item, missingImageListener)
  }

  private fun clear() {
    seeLaterShowTitle.text = ""
    seeLaterShowDescription.text = ""
    seeLaterShowNetwork.text = ""
    seeLaterShowRating.text = ""
    seeLaterShowPlaceholder.gone()
    Glide.with(this).clear(seeLaterShowImage)
  }
}
