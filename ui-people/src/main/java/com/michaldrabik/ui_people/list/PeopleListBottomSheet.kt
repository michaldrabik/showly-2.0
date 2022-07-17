package com.michaldrabik.ui_people.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.FastLinearLayoutManager
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.requireLong
import com.michaldrabik.ui_base.utilities.extensions.requireSerializable
import com.michaldrabik.ui_base.utilities.extensions.requireString
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_DEPARTMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TITLE
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.databinding.ViewPeopleListBinding
import com.michaldrabik.ui_people.details.PersonDetailsBottomSheet
import com.michaldrabik.ui_people.list.recycler.PeopleListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PeopleListBottomSheet : BaseBottomSheetFragment(R.layout.view_people_list) {

  companion object {
    fun createBundle(
      mediaIdTrakt: IdTrakt,
      mediaTitle: String,
      mode: Mode,
      department: Person.Department
    ) = bundleOf(
      ARG_ID to mediaIdTrakt.id,
      ARG_TITLE to mediaTitle,
      ARG_TYPE to mode.type,
      ARG_DEPARTMENT to department,
    )
  }

  private val viewModel by viewModels<PeopleListViewModel>()
  private val binding by viewBinding(ViewPeopleListBinding::bind)

  private val mediaIdTrakt by lazy { IdTrakt(requireLong(ARG_ID)) }
  private val mediaTitle by lazy { requireString(ARG_TITLE) }
  private val mode by lazy { Mode.fromType(requireString(ARG_TYPE)) }
  private val department by lazy { requireSerializable<Person.Department>(ARG_DEPARTMENT) }

  private var adapter: PeopleListAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setupView()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = {
        viewModel.loadPeople(
          mediaIdTrakt,
          mediaTitle,
          mode,
          department
        )
      }
    )
  }

  private fun setupView() {
    val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
    with(behavior) {
      peekHeight = (screenHeight() * 0.45).toInt()
      skipCollapsed = true
      state = BottomSheetBehavior.STATE_COLLAPSED
    }
  }

  private fun setupRecycler() {
    layoutManager = FastLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = PeopleListAdapter(
      onItemClickListener = { openDetails(it) },
    )
    with(binding.viewPeopleListRecycler) {
      adapter = this@PeopleListBottomSheet.adapter
      layoutManager = this@PeopleListBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun openDetails(item: Person) {
    val bundle = PersonDetailsBottomSheet.createBundle(item, mediaIdTrakt)
    findNavController().navigate(R.id.actionPeopleListDialogToDetails, bundle)
  }

  private fun render(uiState: PeopleListUiState) {
    uiState.run {
      peopleItems?.let { adapter?.setItems(it) }
    }
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
