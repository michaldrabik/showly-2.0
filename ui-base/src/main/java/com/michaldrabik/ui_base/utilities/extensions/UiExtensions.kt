package com.michaldrabik.ui_base.utilities.extensions

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.widget.TextView
import androidx.annotation.Px
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toRect
import androidx.core.view.doOnLayout
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import timber.log.Timber
import java.util.Locale

fun View.visible() {
  if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.gone() {
  if (visibility != View.GONE) visibility = View.GONE
}

fun View.invisible() {
  if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.visibleIf(condition: Boolean, gone: Boolean = true) =
  if (condition) {
    visible()
  } else {
    if (gone) gone() else invisible()
  }

fun View.fadeIf(
  condition: Boolean,
  duration: Long = 250,
  startDelay: Long = 0,
  hardware: Boolean = false,
) = if (condition) {
  fadeIn(duration, startDelay, hardware)
} else {
  fadeOut(duration, startDelay, hardware)
}

fun View.fadeIn(
  duration: Long = 250,
  startDelay: Long = 0,
  withHardware: Boolean = false,
  endAction: () -> Unit = {},
): ViewPropertyAnimator? {
  if (visibility == View.VISIBLE) {
    endAction()
    return null
  }
  visibility = View.VISIBLE
  alpha = 0F
  val animation = animate()
    .alpha(1F)
    .setDuration(duration)
    .setStartDelay(startDelay)
    .apply { if (withHardware) withLayer() }
    .withEndAction(endAction)
  return animation.also { it.start() }
}

fun View.fadeOut(
  duration: Long = 250,
  startDelay: Long = 0,
  withHardware: Boolean = false,
  endAction: () -> Unit = {},
): ViewPropertyAnimator? {
  if (visibility == View.GONE) {
    endAction()
    return null
  }
  val animation = animate()
    .alpha(0F)
    .setDuration(duration)
    .setStartDelay(startDelay)
    .apply { if (withHardware) withLayer() }
    .withEndAction {
      gone()
      endAction()
    }
  return animation.also { it.start() }
}

fun ViewPropertyAnimator?.add(animations: MutableList<ViewPropertyAnimator?>): ViewPropertyAnimator? {
  animations.add(this)
  return this
}

fun Animator?.add(animators: MutableList<Animator?>) {
  animators.add(this)
}

fun View.shake() = ObjectAnimator.ofFloat(this, "translationX", 0F, -15F, 15F, -10F, 10F, -5F, 5F, 0F)
  .setDuration(500)
  .start()

fun View.bump(
  duration: Long = 250,
  startDelay: Long = 0,
  action: () -> Unit = {}
) {
  val x = ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.1F, 1F)
  val y = ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.1F, 1F)

  AnimatorSet().apply {
    playTogether(x, y)
    this.startDelay = startDelay
    this.duration = duration
    doOnEnd { action() }
    start()
  }
}

fun View.updateTopMargin(margin: Int) {
  (layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = margin)
}

fun TextView.setTextFade(text: String, duration: Long = 125) {
  fadeOut(
    duration = duration,
    endAction = {
      setText(text)
      fadeIn(duration = duration)
    }
  )
}

fun TextView.isSpoilerHidden(): Boolean {
  return text.toString()
    .trim()
    .replace(" ", "")
    .none { it.isLetterOrDigit() }
}

fun Fragment.disableUi() {
  activity?.window?.setFlags(FLAG_NOT_TOUCHABLE, FLAG_NOT_TOUCHABLE)
  Timber.d("UI disabled.")
}

fun Fragment.enableUi() {
  activity?.window?.clearFlags(FLAG_NOT_TOUCHABLE)
  Timber.d("UI enabled.")
}

fun String.capitalizeWords() = this
  .split(" ")
  .joinToString(separator = " ") {
    it.replaceFirstChar { string -> if (string.isLowerCase()) string.titlecase(Locale.getDefault()) else string.toString() }
  }

fun String.trimWithSuffix(length: Int, suffix: String): String {
  if (this.length <= length) return this
  return this.take(length).plus(suffix)
}

fun View.setOutboundRipple(
  @Px size: Float = 0F,
  @Px corner: Float = 0F,
) {
  val color = context.colorFromAttr(android.R.attr.colorControlHighlight)
  doOnLayout { view ->
    val boundsF = RectF(-size, -size, (view.width + size), (view.height + size))
    val path = Path().apply {
      addRoundRect(boundsF, corner, corner, Path.Direction.CW)
    }
    background = object : RippleDrawable(
      ColorStateList.valueOf(color),
      null,
      null
    ) {
      override fun draw(canvas: Canvas) {
        canvas.clipPath(path)
        super.draw(canvas)
      }
    }.apply {
      val bounds = boundsF.toRect()
      setHotspotBounds(
        bounds.left,
        bounds.top,
        bounds.right,
        bounds.bottom
      )
    }
  }
}
