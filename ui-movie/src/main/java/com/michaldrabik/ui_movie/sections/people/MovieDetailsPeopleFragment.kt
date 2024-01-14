package com.michaldrabik.ui_movie.sections.people

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
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPeopleSheet
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPersonSheet
import com.michaldrabik.ui_movie.MovieDetailsFragment
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.FragmentMovieDetailsPeopleBinding
import com.michaldrabik.ui_movie.sections.people.recycler.ActorsAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON_ARGS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_DETAILS
import com.michaldrabik.ui_people.details.PersonDetailsArgs
import com.michaldrabik.ui_people.details.PersonDetailsBottomSheet
import com.michaldrabik.ui_people.list.PeopleListBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsPeopleFragment : BaseFragment<MovieDetailsPeopleViewModel>(R.layout.fragment_movie_details_people) {

  override val navigationId = R.id.movieDetailsFragment

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsPeopleViewModel>()
  private val binding by viewBinding(FragmentMovieDetailsPeopleBinding::bind)

  private var actorsAdapter: ActorsAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentMovieState.collect { it?.let { viewModel.loadPeople(it) } } },
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadLastPerson() }
    )
  }

  private fun setupView() {
    actorsAdapter = ActorsAdapter().apply {
      itemClickListener = { viewModel.loadPersonDetails(it) }
    }
    binding.movieDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, LinearLayoutManager.HORIZONTAL)
    }
  }

  private fun openPersonSheet(movie: Movie, person: Person, personArgs: PersonDetailsArgs?) {
    handleSheetResult()
    val bundle = PersonDetailsBottomSheet.createBundle(person, movie.ids.trakt, personArgs)
    (requireParentFragment() as BaseFragment<*>)
      .navigateToSafe(R.id.actionMovieDetailsFragmentToPerson, bundle)
  }

  private fun openPeopleSheet(event: OpenPeopleSheet) {
    val (movie, people, department) = event

    if (people.isEmpty()) return
    if (people.size == 1) {
      viewModel.loadPersonDetails(people.first())
      return
    }

    handleSheetResult()

    val title = (requireParentFragment() as MovieDetailsFragment).binding.movieDetailsTitle.text.toString()
    val bundle = PeopleListBottomSheet.createBundle(movie.ids.trakt, title, Mode.MOVIES, department)
    navigateToSafe(R.id.actionMovieDetailsFragmentToPeopleList, bundle)
  }

  private fun render(uiState: MovieDetailsPeopleUiState) {
    with(uiState) {
      with(binding) {
        actors?.let {
          if (actorsAdapter?.itemCount != 0) return@let
          actorsAdapter?.setItems(it)
          movieDetailsActorsRecycler.visibleIf(actors.isNotEmpty(), gone = false)
          movieDetailsActorsEmptyView.visibleIf(actors.isEmpty())
        }
        crew?.let { renderCrew(it) }
        isLoading.let {
          movieDetailsActorsProgress.visibleIf(it)
        }
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
      renderPeople(movieDetailsDirectingLabel, movieDetailsDirectingValue, directors, Department.DIRECTING)
      renderPeople(movieDetailsWritingLabel, movieDetailsWritingValue, writers, Department.WRITING)
      renderPeople(movieDetailsMusicLabel, movieDetailsMusicValue, sound, Department.SOUND)
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPersonSheet -> openPersonSheet(event.movie, event.person, event.personArgs)
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
