package com.michaldrabik.ui_movie.sections.people

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.clearFragmentResultListener
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
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPeopleSheet
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPersonSheet
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.sections.people.recycler.ActorsAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_people.details.PersonDetailsBottomSheet
import com.michaldrabik.ui_people.list.PeopleListBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_details.*
import kotlinx.android.synthetic.main.fragment_movie_details_people.*

@AndroidEntryPoint
class MovieDetailsPeopleFragment : BaseFragment<MovieDetailsPeopleViewModel>(R.layout.fragment_movie_details_people) {

  override val navigationId = R.id.movieDetailsFragment

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsPeopleViewModel>()

  private var actorsAdapter: ActorsAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentEvents.collect { viewModel.handleEvent(it) } },
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
    movieDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, LinearLayoutManager.HORIZONTAL)
    }
  }

  private fun openPersonSheet(movie: Movie, person: Person) {
    val bundle = PersonDetailsBottomSheet.createBundle(person, movie.ids.trakt)
    navigateToSafe(R.id.actionMovieDetailsFragmentToPerson, bundle)
  }

  private fun openPeopleSheet(event: OpenPeopleSheet) {
    val (movie, people, department) = event

    if (people.isEmpty()) return
    if (people.size == 1) {
      viewModel.loadPersonDetails(people.first())
      return
    }

    clearFragmentResultListener(NavigationArgs.REQUEST_PERSON_DETAILS)
    val title = requireParentFragment().movieDetailsTitle.text.toString()
    val bundle = PeopleListBottomSheet.createBundle(movie.ids.trakt, title, Mode.MOVIES, department)
    navigateToSafe(R.id.actionMovieDetailsFragmentToPeopleList, bundle)
  }

  private fun render(uiState: MovieDetailsPeopleUiState) {
    with(uiState) {
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

  private fun renderCrew(crew: Map<Department, List<Person>>) {

    fun renderPeople(
      labelView: View,
      valueView: TextView,
      people: List<Person>,
      department: Department
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

    renderPeople(movieDetailsDirectingLabel, movieDetailsDirectingValue, directors, Department.DIRECTING)
    renderPeople(movieDetailsWritingLabel, movieDetailsWritingValue, writers, Department.WRITING)
    renderPeople(movieDetailsMusicLabel, movieDetailsMusicValue, sound, Department.SOUND)
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPersonSheet -> openPersonSheet(event.movie, event.person)
      is OpenPeopleSheet -> openPeopleSheet(event)
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    actorsAdapter = null
    super.onDestroyView()
  }
}
