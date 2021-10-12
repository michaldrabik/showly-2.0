package com.michaldrabik.ui_base.utilities.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

fun Fragment.launchAndRepeatStarted(
  vararg launchBlock: suspend () -> Unit,
  afterBlock: (() -> Unit)? = null
) {
  viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
      launchBlock.forEach {
        launch { it.invoke() }
      }
      afterBlock?.invoke()
    }
  }
}
