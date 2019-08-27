package com.michaldrabik.showly2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

  @Inject lateinit var viewModelFactory: ViewModelFactory
  protected lateinit var viewModel: T

  protected abstract fun createViewModel(factory: ViewModelFactory): T

  protected abstract fun getLayoutResId(): Int

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewModel = createViewModel(viewModelFactory)
    return inflater.inflate(getLayoutResId(), container, false)
  }
}
