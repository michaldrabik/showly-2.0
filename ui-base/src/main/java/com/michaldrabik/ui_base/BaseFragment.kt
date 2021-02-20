package com.michaldrabik.ui_base

import android.content.Context
import android.os.Bundle
import android.view.ViewPropertyAnimator
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.di.DaggerViewModelFactory
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.ERROR
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.INFO
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_model.Tip
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel<out UiModel>>(@LayoutRes contentLayoutId: Int) :
  Fragment(contentLayoutId),
  TipsHost {

  @Inject lateinit var viewModelFactory: DaggerViewModelFactory
  protected abstract val viewModel: T

  protected var isInitialized = false
  protected val animations = mutableListOf<ViewPropertyAnimator?>()

  protected var mode: Mode
    get() = (requireActivity() as NavigationHost).getMode()
    set(value) = (requireActivity() as NavigationHost).setMode(value)

  protected val moviesEnabled: Boolean
    get() = (requireActivity() as NavigationHost).moviesEnabled()

  protected fun findNavControl() =
    (requireActivity() as NavigationHost).findNavControl()

  protected fun hideNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).hideNavigation(animate)

  protected fun showNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).showNavigation(animate)

  protected fun showSnack(message: MessageEvent) {
    message.consume()?.let {
      val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
      when (message.type) {
        INFO -> host.showInfoSnackbar(getString(it))
        ERROR -> host.showErrorSnackbar(getString(it))
      }
    }
  }

  protected fun navigateTo(@IdRes destination: Int, bundle: Bundle? = null) =
    findNavControl()?.navigate(destination, bundle)

  override fun isTipShown(tip: Tip) = (requireActivity() as TipsHost).isTipShown(tip)

  override fun showTip(tip: Tip) = (requireActivity() as TipsHost).showTip(tip)

  protected fun isTraktSyncing() = (requireAppContext() as OnTraktSyncListener).isTraktSyncActive()

  override fun onDestroyView() {
    animations.forEach { it?.cancel() }
    animations.clear()
    super.onDestroyView()
  }

  fun Fragment.requireAppContext(): Context = requireContext().applicationContext
}
