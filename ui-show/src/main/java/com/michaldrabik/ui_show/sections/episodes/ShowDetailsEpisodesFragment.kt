package com.michaldrabik.ui_show.sections.episodes

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsEpisodesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class ShowDetailsEpisodesFragment : BaseFragment<ShowDetailsEpisodesViewModel>(R.layout.fragment_show_details_episodes) {

  companion object {
    fun createBundle(): Bundle = Bundle.EMPTY

    fun createBundle(show: Show): Bundle =
      bundleOf(NavigationArgs.ARG_OPTIONS to Options(show.ids.trakt))
  }

  private val binding by viewBinding(FragmentShowDetailsEpisodesBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsEpisodesViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
      { parentViewModel.parentEvents.collect { viewModel.handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    with(binding) {
      episodesBackArrow.onClick { requireActivity().onBackPressed() }
    }
  }

  private fun setupStatusBar() {
    with(binding) {
      episodesRoot.doOnApplyWindowInsets { _, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        episodesRoot.updatePadding(top = padding.top + inset)
      }
    }
  }

  private fun render(uiState: ShowDetailsEpisodesUiState) {
  }

  @Parcelize
  data class Options(val id: IdTrakt) : Parcelable
}
