package com.michaldrabik.showly2.ui.followedshows.myshows.views.section

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.section.MyShowsSectionAdapter
import com.michaldrabik.showly2.utilities.extensions.addDivider
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.view_my_shows_section.view.*

class MyShowsSectionView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val padding by lazy { context.dimenToPx(R.dimen.spaceMedium) }
  private val sectionAdapter by lazy { MyShowsSectionAdapter() }
  private val layoutManager by lazy { LinearLayoutManager(context, HORIZONTAL, false) }

  var scrollPositionListener: ((MyShowsSection, Pair<Int, Int>) -> Unit)? = null
  private lateinit var section: MyShowsSection

  init {
    inflate(context, R.layout.view_my_shows_section, this)
    id = View.generateViewId()
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  private fun setupRecycler() {
    myShowsSectionRecycler.apply {
      clearOnScrollListeners()
      setHasFixedSize(true)
      adapter = sectionAdapter
      layoutManager = this@MyShowsSectionView.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      addDivider(R.drawable.divider_my_shows_horizontal, HORIZONTAL)
    }
  }

  fun bind(
    section: MyShowsItem.HorizontalSection,
    scrollPosition: Pair<Int, Int>,
    notifyListsUpdate: Boolean,
    clickListener: (MyShowsItem) -> Unit,
    sectionImageListener: ((MyShowsItem, MyShowsItem.HorizontalSection, Boolean) -> Unit)?
  ) {
    this.section = section.section
    sectionAdapter.run {
      setItems(section.items, notifyChange = notifyListsUpdate)
      itemClickListener = { clickListener(it) }
      missingImageListener = { item, force -> sectionImageListener?.invoke(item, section, force) }
      listChangeListener = { myShowsSectionRecycler.scrollToPosition(0) }
    }
    restoreScrollPosition(scrollPosition)
  }

  private fun saveScrollPosition() {
    val lm = this@MyShowsSectionView.layoutManager
    val position = lm.findFirstVisibleItemPosition()
    val offset = (lm.findViewByPosition(position)?.left ?: 0) - padding
    scrollPositionListener?.invoke(section, Pair(position, offset))
  }

  private fun restoreScrollPosition(scrollPosition: Pair<Int, Int>) {
    val (position, offset) = scrollPosition
    if (position != 0) {
      layoutManager.scrollToPositionWithOffset(position, offset)
      scrollPositionListener?.invoke(section, Pair(0, 0))
    }
  }

  override fun onDetachedFromWindow() {
    saveScrollPosition()
    super.onDetachedFromWindow()
  }
}
