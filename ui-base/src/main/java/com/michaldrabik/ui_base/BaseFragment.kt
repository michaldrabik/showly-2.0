package com.michaldrabik.ui_base

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.ViewPropertyAnimator
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.utilities.ModeHost
import com.michaldrabik.ui_base.utilities.MoviesStatusHost
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_model.Tip

abstract class BaseFragment<T : ViewModel>(@LayoutRes contentLayoutId: Int) :
  Fragment(contentLayoutId),
  TipsHost {

  protected abstract val viewModel: T
  open val navigationId: Int = 0

  protected var isInitialized = false

  protected val animations = mutableListOf<ViewPropertyAnimator?>()
  protected val animators = mutableListOf<Animator?>()
  protected val snackbars = mutableListOf<Snackbar?>()

  protected var mode: Mode
    get() = (requireActivity() as ModeHost).getMode()
    set(value) = (requireActivity() as ModeHost).setMode(value)

  protected val moviesEnabled: Boolean
    get() = (requireActivity() as MoviesStatusHost).hasMoviesEnabled()

  override fun onResume() {
    super.onResume()
    setupBackPressed()
  }

  protected fun findNavControl() =
    (requireActivity() as NavigationHost).findNavControl()

  protected fun hideNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).hideNavigation(animate)

  protected fun showNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).showNavigation(animate)

  protected fun showSnack(message: MessageEvent) {
    val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
    when (message) {
      is MessageEvent.Info -> {
        val length = if (message.isIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT
        val action = if (message.isIndefinite) ({}) else null
        host.showInfoSnackbar(getString(message.textRestId), length = length, action = action)
      }
      is MessageEvent.Error -> host.showErrorSnackbar(getString(message.textRestId))
    }
  }

  protected open fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      isEnabled = false
      findNavControl()?.popBackStack()
    }
  }

  protected fun navigateTo(@IdRes destination: Int, bundle: Bundle? = null) {
    findNavControl()?.navigate(destination, bundle)
  }

  override fun isTipShown(tip: Tip) = (requireActivity() as TipsHost).isTipShown(tip)

  override fun showTip(tip: Tip) = (requireActivity() as TipsHost).showTip(tip)

  override fun setTipShow(tip: Tip) = (requireActivity() as TipsHost).showTip(tip)

  private fun clearAnimations() {
    animations.forEach { it?.cancel() }
    animators.forEach { it?.cancel() }
    animations.clear()
    animators.clear()
  }

  override fun onDestroyView() {
    snackbars.forEach { it?.dismiss() }
    clearAnimations()
    super.onDestroyView()
  }

  fun Fragment.requireAppContext(): Context = requireContext().applicationContext
}
