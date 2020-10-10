package com.michaldrabik.ui_base

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

abstract class BaseFragment<T : BaseViewModel<out UiModel>>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId), TipsHost {

  @Inject
  lateinit var viewModelFactory: DaggerViewModelFactory
  protected abstract val viewModel: T
  protected var isInitialized = false

  protected fun hideNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).hideNavigation(animate)

  protected fun showNavigation(animate: Boolean = true) =
    (requireActivity() as NavigationHost).showNavigation(animate)

  protected fun showSnack(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        INFO -> getSnackbarHost().showInfoSnackbar(getString(it))
        ERROR -> getSnackbarHost().showErrorSnackbar(getString(it))
      }
    }
  }

  protected fun navigateTo(@IdRes destination: Int, bundle: Bundle? = null) =
    findNavController().navigate(destination, bundle)

  protected open fun getSnackbarHost(): ViewGroup = (requireActivity() as SnackbarHost).provideSnackbarLayout()

  override fun isTipShown(tip: Tip) = (requireActivity() as TipsHost).isTipShown(tip)

  override fun showTip(tip: Tip) = (requireActivity() as TipsHost).showTip(tip)

  fun Fragment.requireAppContext(): Context = requireContext().applicationContext
}
