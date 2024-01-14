package com.michaldrabik.ui_show.sections.people

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.trimWithSuffix
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON_ARGS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_DETAILS
import com.michaldrabik.ui_people.details.PersonDetailsArgs
import com.michaldrabik.ui_people.details.PersonDetailsBottomSheet
import com.michaldrabik.ui_people.list.PeopleListBottomSheet
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsEvent.OpenPeopleSheet
import com.michaldrabik.ui_show.ShowDetailsEvent.OpenPersonSheet
import com.michaldrabik.ui_show.ShowDetailsFragment
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsPeopleBinding
import com.michaldrabik.ui_show.sections.people.recycler.ActorsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowDetailsPeopleFragment : BaseFragment<ShowDetailsPeopleViewModel>(R.layout.fragment_show_details_people) {

  override val navigationId = R.id.showDetailsFragment
  private val binding by viewBinding(FragmentShowDetailsPeopleBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsPeopleViewModel>()

  private var actorsAdapter: ActorsAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentShowState.collect { it?.let { viewModel.loadPeople(it) } } },
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadLastPerson() }
    )
  }

  private fun setupView() {
    actorsAdapter = ActorsAdapter().apply {
      itemClickListener = { viewModel.loadPersonDetails(it) }
    }
    binding.showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, LinearLayoutManager.HORIZONTAL)
    }
  }

  private fun openPersonSheet(show: Show, person: Person, personArgs: PersonDetailsArgs?) {
    handleSheetResult()
    val bundle = PersonDetailsBottomSheet.createBundle(person, show.ids.trakt, personArgs)
    (requireParentFragment() as BaseFragment<*>)
      .navigateToSafe(R.id.actionShowDetailsFragmentToPerson, bundle)
  }

  private fun openPeopleSheet(event: OpenPeopleSheet) {
    val (show, people, department) = event

    if (people.isEmpty()) return
    if (people.size == 1) {
      viewModel.loadPersonDetails(people.first())
      return
    }

    handleSheetResult()

    val title = (requireParentFragment() as ShowDetailsFragment).binding.showDetailsTitle.text.toString()
    val bundle = PeopleListBottomSheet.createBundle(show.ids.trakt, title, Mode.SHOWS, department)
    navigateToSafe(R.id.actionShowDetailsFragmentToPeopleList, bundle)
  }

  private fun render(uiState: ShowDetailsPeopleUiState) {
    with(uiState) {
      actors?.let {
        if (actorsAdapter?.itemCount != 0) return@let
        actorsAdapter?.setItems(it)
        binding.showDetailsActorsRecycler.visibleIf(actors.isNotEmpty(), gone = false)
        binding.showDetailsActorsEmptyView.visibleIf(actors.isEmpty())
      }
      crew?.let { renderCrew(it) }
      isLoading.let {
        binding.showDetailsActorsProgress.visibleIf(it)
      }
    }
  }

  private fun renderCrew(crew: Map<Department, List<Person>>) {

    fun renderPeople(
      labelView: View,
      valueView: TextView,
      people: List<Person>,
      department: Department,
    ) {
      labelView.visibleIf(people.isNotEmpty())
      valueView.visibleIf(people.isNotEmpty())
      valueView.text = people
        .take(2)
        .joinToString("\n") { it.name.trimWithSuffix(20, "…") }
        .plus(if (people.size > 2) "\n…" else "")
      valueView.onClick { viewModel.loadPeopleList(people, department) }
    }

    if (!crew.containsKey(Department.DIRECTING)) {
      return
    }

    val directors = crew[Department.DIRECTING] ?: emptyList()
    val writers = crew[Department.WRITING] ?: emptyList()
    val sound = crew[Department.SOUND] ?: emptyList()

    with(binding) {
      renderPeople(showDetailsDirectingLabel, showDetailsDirectingValue, directors, Department.DIRECTING)
      renderPeople(showDetailsWritingLabel, showDetailsWritingValue, writers, Department.WRITING)
      renderPeople(showDetailsMusicLabel, showDetailsMusicValue, sound, Department.SOUND)
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPersonSheet -> openPersonSheet(event.show, event.person, event.personArgs)
      is OpenPeopleSheet -> openPeopleSheet(event)
    }
  }

  @Suppress("DEPRECATION")
  private fun handleSheetResult() {
    requireParentFragment()
      .setFragmentResultListener(REQUEST_DETAILS) { _, bundle ->
        val person = bundle.getParcelable<Person>(ARG_PERSON)
        val personArgs = bundle.getParcelable<PersonDetailsArgs>(ARG_PERSON_ARGS)
        person?.let {
          viewModel.saveLastPerson(it, personArgs)
          bundle.clear()
        }
        requireParentFragment().clearFragmentResultListener(REQUEST_DETAILS)
      }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    actorsAdapter = null
    super.onDestroyView()
  }
}
