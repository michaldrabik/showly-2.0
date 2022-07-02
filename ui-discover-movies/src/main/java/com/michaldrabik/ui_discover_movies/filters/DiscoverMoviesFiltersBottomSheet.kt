package com.michaldrabik.ui_discover_movies.filters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_discover_movies.R
import com.michaldrabik.ui_discover_movies.databinding.ViewDiscoverMoviesFiltersSheetBinding
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.Genre
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
internal class DiscoverMoviesFiltersBottomSheet : BaseBottomSheetFragment(R.layout.view_discover_movies_filters_sheet) {

  companion object {
    const val REQUEST_DISCOVER_FILTERS = "REQUEST_DISCOVER_MOVIES_FILTERS"
  }

  private val viewModel by viewModels<DiscoverMoviesFiltersViewModel>()
  private val binding by viewBinding(ViewDiscoverMoviesFiltersSheetBinding::bind)

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } }
    )
  }

  @SuppressLint("SetTextI18n")
  private fun setupView() {
    val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
    behavior.skipCollapsed = true
    behavior.maxHeight = (screenHeight() * 0.9).toInt()

    with(binding) {
      applyButton.onClick { saveFilters() }
    }
  }

  private fun saveFilters() {
    with(binding) {
      val hideAnticipated = anticipatedSwitch.isChecked
      val hideCollection = collectionSwitch.isChecked

      val feedOrder = when {
        feedChipHot.isChecked -> DiscoverSortOrder.HOT
        feedChipTopRated.isChecked -> DiscoverSortOrder.RATING
        feedChipRecent.isChecked -> DiscoverSortOrder.NEWEST
        else -> throw IllegalStateException()
      }

      val genres = mutableListOf<Genre>().apply {
        genresChipGroup.forEach { chip ->
          if ((chip as Chip).isChecked) {
            add(Genre.valueOf(chip.tag.toString()))
          }
        }
      }

      val filters = DiscoverFilters(
        feedOrder = feedOrder,
        hideAnticipated = hideAnticipated,
        hideCollection = hideCollection,
        genres = genres.toList()
      )

      viewModel.saveFilters(filters)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: DiscoverMoviesFiltersUiState) {
    with(binding) {
      with(uiState) {
        filters?.run {
          feedChipHot.isChecked = feedOrder == DiscoverSortOrder.HOT
          feedChipTopRated.isChecked = feedOrder == DiscoverSortOrder.RATING
          feedChipRecent.isChecked = feedOrder == DiscoverSortOrder.NEWEST
          anticipatedSwitch.isChecked = hideAnticipated
          collectionSwitch.isChecked = hideCollection
          renderGenres(genres)
        }
      }
    }
  }

  private fun renderGenres(genres: List<Genre>) {
    binding.genresChipGroup.removeAllViews()

    val genresNames = genres.map { it.name }
    Genre.values()
      .sortedBy { requireContext().getString(it.displayName) }
      .forEach { genre ->
        val chip = Chip(requireContext()).apply {
          tag = genre.name
          text = requireContext().getString(genre.displayName)
          isCheckable = true
          isCheckedIconVisible = false
          setEnsureMinTouchTargetSize(false)
          chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.selector_discover_chip_background)
          setChipStrokeColorResource(R.color.selector_discover_chip_text)
          setChipStrokeWidthResource(R.dimen.discoverFilterChipStroke)
          setTextColor(ContextCompat.getColorStateList(context, R.color.selector_discover_chip_text))
          isChecked = genre.name in genresNames
        }
        binding.genresChipGroup.addView(chip)
      }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is DiscoverMoviesFiltersUiEvent.ApplyFilters -> {
        setFragmentResult(REQUEST_DISCOVER_FILTERS, Bundle.EMPTY)
        closeSheet()
      }
      is DiscoverMoviesFiltersUiEvent.CloseFilters -> closeSheet()
    }
  }
}
