package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_people.databinding.ViewPersonDetailsFiltersBinding

class PersonDetailsFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewPersonDetailsFiltersBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    with(binding) {
      viewPersonDetailsFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
      viewPersonDetailsFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    }
  }

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null
  private var isListenerEnabled = true

  private fun onChipCheckChange() {
    if (!isListenerEnabled) return
    with(binding) {
      val ids = viewPersonDetailsFiltersChipGroup.checkedChipIds.map {
        when (it) {
          viewPersonDetailsFiltersShowsChip.id -> Mode.SHOWS
          viewPersonDetailsFiltersMoviesChip.id -> Mode.MOVIES
          else -> throw IllegalStateException()
        }
      }
      onChipsChangeListener?.invoke(ids)
    }
  }

  override fun setEnabled(enabled: Boolean) {
    with(binding) {
      viewPersonDetailsFiltersShowsChip.isEnabled = enabled
      viewPersonDetailsFiltersMoviesChip.isEnabled = enabled
    }
  }

  fun bind(types: List<Mode>) {
    with(binding) {
      isListenerEnabled = false
      viewPersonDetailsFiltersShowsChip.isChecked = Mode.SHOWS in types
      viewPersonDetailsFiltersMoviesChip.isChecked = Mode.MOVIES in types
      isListenerEnabled = true
    }
  }
}
