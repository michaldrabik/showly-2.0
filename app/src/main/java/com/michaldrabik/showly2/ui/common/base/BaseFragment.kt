package com.michaldrabik.showly2.ui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.michaldrabik.showly2.ui.ViewModelFactory
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.main.MainActivity
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel<out UiModel>> : Fragment() {

  @Inject lateinit var viewModelFactory: ViewModelFactory
  protected lateinit var viewModel: T

  protected abstract val layoutResId: Int

  protected abstract fun createViewModel(): T

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = createViewModel()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(layoutResId, container, false)

  protected fun getMainActivity() = requireActivity() as MainActivity

  protected open fun onBackPressed() = Unit
}
