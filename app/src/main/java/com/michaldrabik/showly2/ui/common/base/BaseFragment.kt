package com.michaldrabik.showly2.ui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.michaldrabik.showly2.ui.MainActivity
import com.michaldrabik.showly2.ui.ViewModelFactory
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

  @Inject lateinit var viewModelFactory: ViewModelFactory
  protected lateinit var viewModel: T

  protected abstract val layoutResId: Int

  protected abstract fun createViewModel(): T

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewModel = createViewModel()
    return inflater.inflate(layoutResId, container, false)
  }

  protected fun getMainActivity() = requireActivity() as MainActivity
}
