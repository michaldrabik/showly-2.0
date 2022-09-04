package com.michaldrabik.ui_discover.filters.genres

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
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_discover.DiscoverFragment.Companion.REQUEST_DISCOVER_FILTERS
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewDiscoverFiltersGenresBinding
import com.michaldrabik.ui_discover.filters.genres.DiscoverFiltersGenresUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.genres.DiscoverFiltersGenresUiEvent.CloseFilters
import com.michaldrabik.ui_model.Genre
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class DiscoverFiltersGenresBottomSheet : BaseBottomSheetFragment(R.layout.view_discover_filters_genres) {

  private val viewModel by viewModels<DiscoverFiltersGenresViewModel>()
  private val binding by viewBinding(ViewDiscoverFiltersGenresBinding::bind)

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
      applyButton.onClick { saveGenres() }
      clearButton.onClick { renderGenres(emptyList()) }
    }
  }

  private fun saveGenres() {
    with(binding) {
      val genres = mutableListOf<Genre>().apply {
        genresChipGroup.forEach { chip ->
          if ((chip as Chip).isChecked) {
            add(Genre.valueOf(chip.tag.toString()))
          }
        }
      }
      viewModel.saveGenres(genres)
    }
  }

  private fun render(uiState: DiscoverFiltersGenresUiState) {
    with(uiState) {
      genres?.let { renderGenres(it) }
    }
  }

  private fun renderGenres(genres: List<Genre>) {
    binding.genresChipGroup.removeAllViews()
    binding.clearButton.visibleIf(genres.isNotEmpty())

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
          shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(100f)
            .build()
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
      is ApplyFilters -> {
        setFragmentResult(REQUEST_DISCOVER_FILTERS, Bundle.EMPTY)
        closeSheet()
      }
      is CloseFilters -> closeSheet()
    }
  }
}
