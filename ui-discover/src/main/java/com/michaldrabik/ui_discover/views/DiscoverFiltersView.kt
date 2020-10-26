package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.google.android.material.chip.Chip
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.Genre
import kotlinx.android.synthetic.main.view_discover_filters.view.*

class DiscoverFiltersView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onApplyClickListener: ((DiscoverFilters) -> Unit)? = null

  init {
    val spaceNormal = context.dimenToPx(R.dimen.spaceNormal)
    val spaceSmall = context.dimenToPx(R.dimen.spaceSmall)

    inflate(context, R.layout.view_discover_filters, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    isClickable = true
    clipChildren = false
    clipToPadding = false
    setBackgroundResource(R.drawable.bg_discover_filters)
    setPadding(spaceNormal, spaceNormal, spaceNormal, spaceSmall)

    discoverFiltersApplyButton.onClick { onApplyFilters() }
    discoverFiltersAnticipatedText.onClick { discoverFiltersAnticipatedSwitch.toggle() }
  }

  fun bind(filters: DiscoverFilters) {
    discoverFiltersChipHot.isChecked = filters.feedOrder == HOT
    discoverFiltersChipTopRated.isChecked = filters.feedOrder == RATING
    discoverFiltersChipMostRecent.isChecked = filters.feedOrder == NEWEST
    discoverFiltersAnticipatedSwitch.isChecked = filters.hideAnticipated
    bindGenres(filters.genres)
  }

  private fun bindGenres(genres: List<Genre>) {
    val genresNames = genres.map { it.name }
    discoverFiltersGenresChipGroup.removeAllViews()
    Genre.values()
      .sortedBy { context.getString(it.displayName) }
      .forEach { genre ->
        val chip = Chip(context).apply {
          tag = genre.name
          text = context.getString(genre.displayName)
          isCheckable = true
          isCheckedIconVisible = false
          setEnsureMinTouchTargetSize(false)
          chipBackgroundColor = context.colorStateListFromAttr(R.attr.colorSearchViewBackground)
          setChipStrokeColorResource(R.color.selector_discover_chip_text)
          setChipStrokeWidthResource(R.dimen.discoverFilterChipStroke)
          setTextColor(ContextCompat.getColorStateList(context, R.color.selector_discover_chip_text))
          isChecked = genre.name in genresNames
        }
        discoverFiltersGenresChipGroup.addView(chip)
      }
  }

  private fun onApplyFilters() {
    val showAnticipated = discoverFiltersAnticipatedSwitch.isChecked

    val feedOrder = when {
      discoverFiltersChipHot.isChecked -> HOT
      discoverFiltersChipTopRated.isChecked -> RATING
      discoverFiltersChipMostRecent.isChecked -> NEWEST
      else -> throw IllegalStateException()
    }

    val genres = mutableListOf<Genre>()
    discoverFiltersGenresChipGroup.forEach { chip ->
      if ((chip as Chip).isChecked) {
        genres.add(Genre.valueOf(chip.tag.toString()))
      }
    }

    val filters = DiscoverFilters(feedOrder, showAnticipated, genres.toList())
    onApplyClickListener?.invoke(filters)
  }
}
