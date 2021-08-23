package com.michaldrabik.ui_base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.michaldrabik.ui_base.utilities.NavigationHost

abstract class BaseBottomSheetFragment<T : BaseViewModel> : BottomSheetDialogFragment() {

  protected lateinit var viewModel: T

  protected abstract val layoutResId: Int

  protected abstract fun createViewModel(): T

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = createViewModel()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(layoutResId, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
  }

  protected fun navigateTo(@IdRes destination: Int, bundle: Bundle? = null) =
    (requireActivity() as NavigationHost).findNavControl()?.navigate(destination, bundle)

  protected fun closeSheet() = (requireActivity() as NavigationHost).findNavControl()?.navigateUp()
}
