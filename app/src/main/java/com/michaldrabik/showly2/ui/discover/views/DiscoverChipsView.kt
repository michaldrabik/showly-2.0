package com.michaldrabik.showly2.ui.discover.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CompoundButton
import android.widget.HorizontalScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Genre
import com.michaldrabik.showly2.ui.common.views.search.DiscoverChipsViewBehaviour
import kotlinx.android.synthetic.main.view_discover_chips.view.*

class DiscoverChipsView : HorizontalScrollView, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    View.inflate(context, R.layout.view_discover_chips, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    isHorizontalScrollBarEnabled = false
    setupChips(Genre.values().toList())
  }

  var selectedChips: List<Genre>
    get() = discoverChipsGroup.children
      .filter { (it as Chip).isChecked }
      .map { it.tag as Genre }
      .toList()
    set(value) {
      isListenerDisabled = true
      discoverChipsGroup.children.forEach { child ->
        (child as Chip).run { isChecked = ((tag as Genre) in value) }
      }
      isListenerDisabled = false
    }

  var onChipsSelectedListener: (List<Genre>) -> Unit = {}
  private var isListenerDisabled = false

  override fun getBehavior() = DiscoverChipsViewBehaviour()

  @SuppressLint("DefaultLocale")
  private fun setupChips(genres: List<Genre>) {
    val color = ResourcesCompat.getColorStateList(resources, R.color.bg_discover_chip, null)
    val checkListener: (CompoundButton, Boolean) -> Unit = { _, _ -> onChipSelected() }

    discoverChipsGroup.removeAllViews()
    genres.forEach { genre ->
      val view = Chip(context).apply {
        layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        isCheckable = true
        isCheckedIconVisible = false
        tag = genre
        text = genre.slug.capitalize()
        setTextColor(color)
        setChipStrokeColorResource(R.color.bg_discover_chip)
        setChipStrokeWidthResource(R.dimen.discoverChipStrokeWidth)
        setChipBackgroundColorResource(R.color.colorBackground)
        setOnCheckedChangeListener(checkListener)
      }
      discoverChipsGroup.addView(view)
    }
  }

  private fun onChipSelected() {
    val genres = discoverChipsGroup.children
      .filter { (it as Chip).isChecked }
      .map { it.tag as Genre }
      .toList()
    if (!isListenerDisabled) onChipsSelectedListener(genres)
  }

  fun clear() {
    isListenerDisabled = true
    scrollTo(0, 0)
    discoverChipsGroup.children.forEach {
      (it as Chip).isChecked = false
    }
    isListenerDisabled = false
  }

  override fun setEnabled(enabled: Boolean) {
    discoverChipsGroup.children.forEach { chip ->
      chip.isEnabled = enabled
      chip.isClickable = enabled
    }
  }
}