package com.michaldrabik.showly2.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.michaldrabik.common.Mode
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_bottom_menu.view.*
import kotlin.math.abs

class BottomMenuView : FrameLayout {

  companion object {
    private const val SWIPE_MIN_THRESHOLD = 150F
    private const val FADE_DELAY = 150L
  }

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_bottom_menu, this)
  }

  var isModeMenuEnabled = true
  var onModeSelected: ((Mode) -> Unit)? = null

  private val screenWidth by lazy { screenWidth() }
  private val itemIdleColor by lazy { context.colorFromAttr(R.attr.colorBottomMenuItem) }
  private val itemSelectedColor by lazy { context.colorFromAttr(R.attr.colorBottomMenuItemChecked) }
  private val animations = mutableListOf<ViewPropertyAnimator?>()

  private var touchX = 0F
  private var isModeMenu = false

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    if (!isModeMenuEnabled) return super.onInterceptTouchEvent(ev)

    when (ev?.actionMasked) {
      ACTION_DOWN -> {
        touchX = ev.x
        isModeMenu = false
        disableTooltips()
      }
      ACTION_MOVE -> {
        val delta = ev.x - touchX
        if (!isModeMenu && abs(delta) > SWIPE_MIN_THRESHOLD) {
          isModeMenu = true
          showModeMenu()
        }
        if (isModeMenu) {
          if (ev.x > screenWidth / 2) {
            bottomMenuModeShows.setTextColor(itemIdleColor)
            bottomMenuModeMovies.setTextColor(itemSelectedColor)
          } else {
            bottomMenuModeShows.setTextColor(itemSelectedColor)
            bottomMenuModeMovies.setTextColor(itemIdleColor)
          }
        }
      }
      ACTION_UP, ACTION_CANCEL -> {
        if (isModeMenu) {
          hideModeMenu()
          isModeMenu = false
          when {
            ev.x > screenWidth / 2 -> onModeSelected?.invoke(Mode.MOVIES)
            else -> onModeSelected?.invoke(Mode.SHOWS)
          }
        }
      }
    }

    return super.onInterceptTouchEvent(ev)
  }

  private fun showModeMenu() {
    bottomMenuModeShows.setTextColor(itemIdleColor)
    bottomMenuModeMovies.setTextColor(itemIdleColor)

    bottomNavigationView.fadeOut(FADE_DELAY).add(animations)
    bottomMenuModeLayout.fadeIn(FADE_DELAY).add(animations)
  }

  private fun hideModeMenu() {
    with(animations) {
      forEach {
        it?.setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationCancel(animation: Animator) {
            bottomNavigationView.visible()
            bottomNavigationView.alpha = 1F
          }
        })
        it?.cancel()
      }
      clear()
    }
    bottomNavigationView.fadeIn(FADE_DELAY).add(animations)
    bottomMenuModeLayout.fadeOut(FADE_DELAY).add(animations)
  }

  private fun disableTooltips() {
    val content = bottomNavigationView.getChildAt(0)
    if (content is ViewGroup) {
      content.forEach {
        if (it is BottomNavigationItemView) {
          it.setOnLongClickListener(null)
        }
      }
    }
  }
}
