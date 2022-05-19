package com.michaldrabik.ui_comments.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class CommentsFragment : BaseFragment<CommentsViewModel>(R.layout.fragment_comments) {

  companion object {
    fun createBundle(movie: Movie): Bundle =
      bundleOf(ARG_OPTIONS to Options(movie.ids.trakt, Mode.MOVIES))

    fun createBundle(show: Show): Bundle =
      bundleOf(ARG_OPTIONS to Options(show.ids.trakt, Mode.SHOWS))

    @Parcelize
    data class Options(
      val id: IdTrakt,
      val mode: Mode,
    ) : Parcelable
  }

  override val viewModel by viewModels<CommentsViewModel>()
  override val navigationId = R.id.commentsFragment

  private val options by lazy { requireParcelable<Options>(ARG_OPTIONS) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
//      { viewModel.uiState.collect { render(it) } },
//      { viewModel.messageFlow.collect { renderSnack(it) } },
//      { viewModel.eventFlow.collect { handleEvent(it) } },
    )
  }

  private fun setupView() {
    hideNavigation()
  }

  private fun setupStatusBar() {
//    movieDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
//      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//      if (imagePadded) {
//        movieDetailsMainLayout
//          .updatePadding(top = inset)
//      } else {
//        (movieDetailsShareButton.layoutParams as ViewGroup.MarginLayoutParams)
//          .updateMargins(top = inset)
//      }
//      arrayOf<View>(view, movieDetailsBackArrow2, movieDetailsCommentsView)
//        .forEach { v ->
//          (v.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = inset)
//        }
//    }
  }
}
