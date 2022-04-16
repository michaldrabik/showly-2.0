package com.michaldrabik.ui_base.utilities.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.NavigationHost
import kotlinx.coroutines.launch

fun Fragment.launchAndRepeatStarted(
  vararg launchBlock: suspend () -> Unit,
  doAfterLaunch: (() -> Unit)? = null
) {
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
      launchBlock.forEach {
        launch { it.invoke() }
      }
      doAfterLaunch?.invoke()
    }
  }
}

fun BaseFragment<*>.navigateTo(
  @IdRes destination: Int,
  bundle: Bundle? = null
) {
  (requireActivity() as NavigationHost).findNavControl()?.navigate(destination, bundle)
}

fun BaseFragment<*>.navigateToSafe(
  @IdRes destination: Int,
  bundle: Bundle? = null
) {
  check(navigationId != 0) { "Navigation ID not provided!" }
  (requireActivity() as NavigationHost).findNavControl()?.let { navController ->
    if (navController.currentDestination?.id == navigationId) {
      navigateTo(destination, bundle)
    }
  }
}
