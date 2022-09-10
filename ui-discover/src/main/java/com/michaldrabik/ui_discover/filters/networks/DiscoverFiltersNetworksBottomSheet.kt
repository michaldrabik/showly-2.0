package com.michaldrabik.ui_discover.filters.networks

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
import com.michaldrabik.ui_discover.databinding.ViewDiscoverFiltersNetworksBinding
import com.michaldrabik.ui_discover.filters.networks.DiscoverFiltersNetworksUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.networks.DiscoverFiltersNetworksUiEvent.CloseFilters
import com.michaldrabik.ui_discover.filters.networks.helpers.NetworkIconProvider
import com.michaldrabik.ui_model.Network
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class DiscoverFiltersNetworksBottomSheet : BaseBottomSheetFragment(R.layout.view_discover_filters_networks) {

  private val viewModel by viewModels<DiscoverFiltersNetworksViewModel>()
  private val binding by viewBinding(ViewDiscoverFiltersNetworksBinding::bind)

  @Inject lateinit var networkIconProvider: NetworkIconProvider

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
      applyButton.onClick { saveNetworks() }
      clearButton.onClick { renderNetworks(emptyList()) }
    }
  }

  private fun saveNetworks() {
    with(binding) {
      val networks = mutableListOf<Network>().apply {
        networksChipGroup.forEach { chip ->
          if ((chip as Chip).isChecked) {
            add(Network.valueOf(chip.tag.toString()))
          }
        }
      }
      viewModel.saveNetworks(networks)
    }
  }

  private fun render(uiState: DiscoverFiltersNetworksUiState) {
    with(uiState) {
      networks?.let { renderNetworks(it) }
    }
  }

  private fun renderNetworks(networks: List<Network>) {
    binding.networksChipGroup.removeAllViews()
    binding.clearButton.visibleIf(networks.isNotEmpty())

    val networksNames = networks.map { it.name }
    Network.values()
      .sortedBy { it.name }
      .forEach { network ->
        val icon = networkIconProvider.getIcon(network)
        val chip = Chip(requireContext()).apply {
          tag = network.name
          text = network.channels.first()
          isCheckable = true
          isCheckedIconVisible = false
          shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(100f)
            .build()
          setEnsureMinTouchTargetSize(false)
          setChipIconResource(icon)
          chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.selector_discover_chip_background)
          setChipStrokeColorResource(R.color.selector_discover_chip_text)
          setChipStrokeWidthResource(R.dimen.discoverFilterChipStroke)
          setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_discover_chip_text))
          isChecked = network.name in networksNames
        }
        binding.networksChipGroup.addView(chip)
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
