package com.michaldrabik.ui_base.common.sheets.ratings

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.RateValueView.Direction
import com.michaldrabik.ui_base.databinding.ViewRateSheetBinding
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RatingsBottomSheet : BaseBottomSheetFragment<RatingsSheetViewModel>() {

  companion object {
    fun createBundle(id: IdTrakt, type: Options.Type): Bundle {
      val options = Options(id, type)
      return bundleOf(NavigationArgs.ARG_OPTIONS to options)
    }

    private const val INITIAL_RATING = 5
  }

  override val layoutResId = R.layout.view_rate_sheet
  private val view by lazy { viewBinding as ViewRateSheetBinding }

  private val options by lazy { (requireArguments().getParcelable<Options>(NavigationArgs.ARG_OPTIONS))!! }
  private val id by lazy { options.id }
  private val type by lazy { options.type }

  private val starsViews by lazy {
    with(view) { listOf(star1, star2, star3, star4, star5, star6, star7, star8, star9, star10) }
  }
  private var selectedRating = INITIAL_RATING

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    val view = inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
    return createViewBinding(ViewRateSheetBinding.bind(view))
  }

  override fun createViewModel() = ViewModelProvider(this)[RatingsSheetViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      doAfterLaunch = { viewModel.loadRating(id, type) }
    )
  }

  private fun setupView() {
    renderRating(INITIAL_RATING)
    starsViews.forEach { star -> star.onClick { renderRating(it.tag.toString().toInt(), animate = true) } }
    view.viewRateSheetSaveButton.onClick { viewModel.saveRating(selectedRating, id, type) }
    view.viewRateSheetRemoveButton.onClick { viewModel.removeRating(id, type) }
  }

  private fun render(uiState: RatingsUiState) {
    with(uiState) {
      with(view) {
        isLoading?.let {
          viewRateSheetProgress.visibleIf(it)
          viewRateSheetSaveButton.visibleIf(!it, gone = false)
          viewRateSheetRemoveButton.visibleIf(!it, gone = false)
          starsViews.forEach { view -> view.isEnabled = !it }
        }
        rating?.let {
          viewRateSheetSaveButton.isEnabled = true
          if (isLoading != true) {
            viewRateSheetRemoveButton.visibleIf(it != TraktRating.EMPTY)
          }
          viewRateSheetStarsLayout.visible()
          if (it != TraktRating.EMPTY && isLoading != true) {
            renderRating(it.rating)
          }
        }
      }
    }
  }

  private fun renderRating(rate: Int, animate: Boolean = false) {
    val currentRating = selectedRating
    selectedRating = rate.coerceIn(1..10)
    starsViews.forEach { it.setImageResource(R.drawable.ic_star_empty) }
    (1..selectedRating).forEachIndexed { index, _ ->
      starsViews[index].setImageResource(R.drawable.ic_star)
    }
    if (animate && currentRating != selectedRating) {
      val direction = if (currentRating > selectedRating) Direction.RIGHT else Direction.LEFT
      view.viewRateSheetRating.setValueAnimated(selectedRating.toString(), direction)
    } else {
      view.viewRateSheetRating.setValue(selectedRating.toString())
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        MessageEvent.Type.INFO -> view.viewRateSheetSnackHost.showInfoSnackbar(getString(it))
        MessageEvent.Type.ERROR -> view.viewRateSheetSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is FinishUiEvent -> closeWithSuccess(event.operation)
    }
  }

  private fun closeWithSuccess(operation: Options.Operation) {
    val result = bundleOf(NavigationArgs.RESULT to operation)
    setFragmentResult(NavigationArgs.REQUEST_RATING, result)
    closeSheet()
  }

  @Parcelize
  data class Options(
    val id: IdTrakt,
    val type: Type,
  ) : Parcelable {

    enum class Type {
      SHOW,
      MOVIE,
      EPISODE,
      SEASON
    }

    @Parcelize
    enum class Operation : Parcelable {
      SAVE,
      REMOVE
    }
  }
}
