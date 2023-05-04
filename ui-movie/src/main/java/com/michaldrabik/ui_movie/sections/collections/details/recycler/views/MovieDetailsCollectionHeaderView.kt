package com.michaldrabik.ui_movie.sections.collections.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_movie.databinding.ViewMovieCollectionListHeaderBinding
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem

class MovieDetailsCollectionHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMovieCollectionListHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.subtitleText.setInitialLines(3)
  }

  fun bind(item: MovieDetailsCollectionItem.HeaderItem) {
    with(binding) {
      titleText.text = item.title
      subtitleText.text = item.description.ifBlank { item.title }
    }
  }
}
