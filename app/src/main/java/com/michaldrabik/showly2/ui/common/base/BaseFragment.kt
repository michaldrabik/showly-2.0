package com.michaldrabik.showly2.ui.common.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.di.DaggerViewModelFactory
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.main.MainActivity
import com.michaldrabik.showly2.utilities.MessageEvent
import com.michaldrabik.showly2.utilities.MessageEvent.Type.ERROR
import com.michaldrabik.showly2.utilities.MessageEvent.Type.INFO
import com.michaldrabik.showly2.utilities.extensions.showErrorSnackbar
import com.michaldrabik.showly2.utilities.extensions.showInfoSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel<out UiModel>>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  @Inject
  lateinit var viewModelFactory: DaggerViewModelFactory
  protected abstract val viewModel: T
  protected var isInitialized = false

  protected fun hideNavigation(animate: Boolean = true) =
    mainActivity().hideNavigation(animate)

  protected fun showNavigation(animate: Boolean = true) =
    mainActivity().showNavigation(animate)

  protected fun showSnack(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        INFO -> getSnackbarHost().showInfoSnackbar(getString(it))
        ERROR -> getSnackbarHost().showErrorSnackbar(getString(it))
      }
    }
  }

  protected open fun getSnackbarHost(): ViewGroup = mainActivity().snackBarHost

  protected fun mainActivity() = requireActivity() as MainActivity

  protected fun navigateTo(@IdRes destination: Int, bundle: Bundle? = null) =
    findNavController().navigate(destination, bundle)
}
