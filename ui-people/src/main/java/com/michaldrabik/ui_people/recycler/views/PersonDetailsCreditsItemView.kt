package com.michaldrabik.ui_people.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.recycler.PersonDetailsItem
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.*

class PersonDetailsCreditsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val spaceNano by lazy { context.dimenToPx(R.dimen.spaceNano).toFloat() }

  init {
    inflate(context, R.layout.view_person_details_credits_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(item: PersonDetailsItem.CreditsShowItem) {
    bindTitleDescription(item.show.title, item.show.overview)

    val year = if (item.show.year > 0) item.show.year.toString() else "TBA"
    viewPersonCreditsItemNetwork.text =
      if (item.show.network.isNotBlank()) context.getString(R.string.textNetwork, year, item.show.network)
      else String.format("%s", year)

    viewPersonCreditsItemPlaceholder.setImageResource(R.drawable.ic_television)
    viewPersonCreditsItemIcon.setImageResource(R.drawable.ic_television)
    viewPersonCreditsItemNetwork.translationY = spaceNano

    bindImage(item.image)
  }

  fun bind(item: PersonDetailsItem.CreditsMovieItem) {
    bindTitleDescription(item.movie.title, item.movie.overview)
    viewPersonCreditsItemNetwork.text = String.format("%s", item.movie.released?.year ?: "TBA")

    viewPersonCreditsItemPlaceholder.setImageResource(R.drawable.ic_film)
    viewPersonCreditsItemIcon.setImageResource(R.drawable.ic_film)
    viewPersonCreditsItemNetwork.translationY = 0F

    bindImage(item.image)
  }

  private fun bindTitleDescription(title: String, description: String) {
    viewPersonCreditsItemTitle.text = title
    viewPersonCreditsItemDescription.text =
      if (description.isNotBlank()) description
      else context.getString(R.string.textNoDescription)
  }

  private fun bindImage(image: Image) {
    viewPersonCreditsItemPlaceholder.visible()
    viewPersonCreditsItemImage.gone()
  }
}
