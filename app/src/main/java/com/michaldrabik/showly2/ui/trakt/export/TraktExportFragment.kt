package com.michaldrabik.showly2.ui.trakt.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.EventObserver
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_trakt_export.*

class TraktExportFragment : BaseFragment<TraktExportViewModel>(), EventObserver {

  override val layoutResId = R.layout.fragment_trakt_export
  override val viewModel by viewModels<TraktExportViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    EventsManager.registerObserver(this)
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onDestroyView() {
    EventsManager.removeObserver(this)
    super.onDestroyView()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showInfoSnackbar(it!!) })
      errorLiveData.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it!!) })
    }
  }

  private fun setupView() {
    traktExportToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    traktExportButton.onClick { startExport() }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun startExport() {
//    val context = requireContext().applicationContext
//    Intent(context, TraktImportService::class.java).run {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        context.startForegroundService(this)
//      } else {
//        context.startService(this)
//      }
//    }
  }

  private fun render(uiModel: TraktExportUiModel) {
    uiModel.run {
      isProgress?.let {
        traktExportButton.visibleIf(!it, false)
        traktExportProgress.visibleIf(it)
      }
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      findNavController().popBackStack()
    }
  }

  override fun getSnackbarHost(): ViewGroup = traktExportRoot

  override fun onNewEvent(event: Event) = viewModel.handleEvent(event)
}
