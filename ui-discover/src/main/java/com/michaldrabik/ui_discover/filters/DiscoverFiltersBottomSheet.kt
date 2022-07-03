package com.michaldrabik.ui_discover.filters

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
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewDiscoverFiltersSheetBinding
import com.michaldrabik.ui_discover.filters.helpers.NetworkIconProvider
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Network
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
internal class DiscoverFiltersBottomSheet : BaseBottomSheetFragment(R.layout.view_discover_filters_sheet) {

  companion object {
    const val REQUEST_DISCOVER_FILTERS = "REQUEST_DISCOVER_FILTERS"
  }

  private val viewModel by viewModels<DiscoverFiltersViewModel>()
  private val binding by viewBinding(ViewDiscoverFiltersSheetBinding::bind)

  @Inject
  lateinit var networkIconProvider: NetworkIconProvider

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
      discoverFiltersApplyButton.onClick { saveFilters() }
    }
  }

  private fun saveFilters() {
    with(binding) {
      val hideAnticipated = discoverFiltersAnticipatedSwitch.isChecked
      val hideCollection = discoverFiltersCollectionSwitch.isChecked

      val feedOrder = when {
        discoverFiltersChipHot.isChecked -> DiscoverSortOrder.HOT
        discoverFiltersChipTopRated.isChecked -> DiscoverSortOrder.RATING
        discoverFiltersChipMostRecent.isChecked -> DiscoverSortOrder.NEWEST
        else -> throw IllegalStateException()
      }

      val genres = mutableListOf<Genre>().apply {
        discoverFiltersGenresChipGroup.forEach { chip ->
          if ((chip as Chip).isChecked) {
            add(Genre.valueOf(chip.tag.toString()))
          }
        }
      }

      val networks = mutableListOf<Network>().apply {
        discoverFiltersNetworksChipGroup.forEach { chip ->
          if ((chip as Chip).isChecked) {
            add(Network.valueOf(chip.tag.toString()))
          }
        }
      }

      val filters = DiscoverFilters(
        feedOrder = feedOrder,
        hideAnticipated = hideAnticipated,
        hideCollection = hideCollection,
        genres = genres.toList(),
        networks = networks.toList()
      )

      viewModel.saveFilters(filters)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: DiscoverFiltersUiState) {
    with(binding) {
      with(uiState) {
        filters?.run {
          discoverFiltersChipHot.isChecked = feedOrder == DiscoverSortOrder.HOT
          discoverFiltersChipTopRated.isChecked = feedOrder == DiscoverSortOrder.RATING
          discoverFiltersChipMostRecent.isChecked = feedOrder == DiscoverSortOrder.NEWEST
          discoverFiltersAnticipatedSwitch.isChecked = hideAnticipated
          discoverFiltersCollectionSwitch.isChecked = hideCollection
          renderGenres(genres)
          renderNetworks(networks)
        }
      }
    }
  }

  private fun renderGenres(genres: List<Genre>) {
    binding.discoverFiltersGenresChipGroup.removeAllViews()

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
        binding.discoverFiltersGenresChipGroup.addView(chip)
      }
  }

  private fun renderNetworks(networks: List<Network>) {
    binding.discoverFiltersNetworksChipGroup.removeAllViews()

    val networksNames = networks.map { it.name }
    Network.values()
      .sortedWith(compareByDescending<Network> { it.name in networksNames } then compareBy { it.name })
      .forEach { network ->
        val icon = networkIconProvider.getIcon(network)
        val chip = Chip(requireContext()).apply {
          tag = network.name
          text = network.channels.first()
          isCheckable = true
          isCheckedIconVisible = false
          setEnsureMinTouchTargetSize(false)
          setChipIconResource(icon)
          chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.selector_discover_chip_background)
          setChipStrokeColorResource(R.color.selector_discover_chip_text)
          setChipStrokeWidthResource(R.dimen.discoverFilterChipStroke)
          setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_discover_chip_text))
          isChecked = network.name in networksNames
        }
        binding.discoverFiltersNetworksChipGroup.addView(chip)
      }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is DiscoverFiltersUiEvent.ApplyFilters -> {
        setFragmentResult(REQUEST_DISCOVER_FILTERS, Bundle.EMPTY)
        closeSheet()
      }
      is DiscoverFiltersUiEvent.CloseFilters -> closeSheet()
    }
  }
}
