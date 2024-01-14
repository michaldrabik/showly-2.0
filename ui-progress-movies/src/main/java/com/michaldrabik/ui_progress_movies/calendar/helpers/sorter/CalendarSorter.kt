package com.michaldrabik.ui_progress_movies.calendar.helpers.sorter

import com.michaldrabik.ui_model.Movie
import java.util.Comparator

interface CalendarSorter {
  fun sort(): Comparator<Movie>
}
