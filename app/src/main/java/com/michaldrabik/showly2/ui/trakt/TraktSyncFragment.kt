package com.michaldrabik.showly2.ui.trakt

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.michaldrabik.network.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.EventObserver
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.trakt.TraktSyncService
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnTraktAuthorizeListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_trakt_sync.*

class TraktSyncFragment : BaseFragment<TraktSyncViewModel>(), OnTraktAuthorizeListener, EventObserver {

  override val layoutResId = R.layout.fragment_trakt_sync
  override val viewModel by viewModels<TraktSyncViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
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
      invalidate()
    }
  }

  private fun setupView() {
    traktSyncToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun startImport() {
    val context = requireContext().applicationContext
    Intent(context, TraktSyncService::class.java).run {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(this)
      } else {
        context.startService(this)
      }
    }
  }

  private fun startAuthorization() {
    Intent(Intent.ACTION_VIEW).run {
      data = Uri.parse(Config.TRAKT_AUTHORIZE_URL)
      startActivity(this)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) = viewModel.authorizeTrakt(authData)

  private fun render(uiModel: TraktSyncUiModel) {
    uiModel.run {
      isProgress?.let {
        traktSyncButton.visibleIf(!it, false)
        traktSyncProgress.visibleIf(it)
      }
      authError?.let { findNavController().popBackStack() }
      isAuthorized?.let {
        when {
          it -> {
            traktSyncButton.text = getString(R.string.textTraktSyncStart)
            traktSyncButton.onClick { startImport() }
          }
          else -> {
            traktSyncButton.text = getString(R.string.textSettingsTraktAuthorizeTitle)
            traktSyncButton.onClick { startAuthorization() }
          }
        }
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

  override fun getSnackbarHost(): ViewGroup = traktSyncRoot

  override fun onNewEvent(event: Event) = viewModel.handleEvent(event)
}
