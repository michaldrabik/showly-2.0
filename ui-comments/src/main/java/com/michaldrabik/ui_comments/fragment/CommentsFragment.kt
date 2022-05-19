package com.michaldrabik.ui_comments.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_comments.databinding.FragmentCommentsBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.collect

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class CommentsFragment : BaseFragment<CommentsViewModel>(R.layout.fragment_comments) {

  override val navigationId = R.id.commentsFragment
  override val viewModel by viewModels<CommentsViewModel>()
  private val binding by viewBinding(FragmentCommentsBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
//      { viewModel.messageFlow.collect { renderSnack(it) } },
//      { viewModel.eventFlow.collect { handleEvent(it) } },
    )
  }

  private fun setupView() {
    hideNavigation()
  }

  private fun setupStatusBar() {
    binding.commentsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + inset)
    }
  }

  private fun render(uiState: CommentsUiState) {
    with(uiState) {

    }
  }

  companion object {
    fun createBundle(movie: Movie): Bundle =
      bundleOf(ARG_OPTIONS to Options(movie.ids.trakt, Mode.MOVIES))

    fun createBundle(show: Show): Bundle =
      bundleOf(ARG_OPTIONS to Options(show.ids.trakt, Mode.SHOWS))
  }

  @Parcelize
  data class Options(val id: IdTrakt, val mode: Mode) : Parcelable
}
