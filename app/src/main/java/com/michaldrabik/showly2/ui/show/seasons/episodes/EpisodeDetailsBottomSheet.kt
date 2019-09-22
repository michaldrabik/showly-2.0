package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import kotlinx.android.synthetic.main.view_episode_details.view.*

class EpisodeDetailsBottomSheet : BottomSheetDialogFragment() {

  companion object {
    private const val ARG_NUMBER = "ARG_NUMBER"
    private const val ARG_SEASON = "ARG_SEASON"
    private const val ARG_TITLE = "ARG_TITLE"
    private const val ARG_OVERVIEW = "ARG_OVERVIEW"

    fun create(episode: Episode): EpisodeDetailsBottomSheet {
      val bundle = Bundle().apply {
        putString(ARG_TITLE, episode.title)
        putString(ARG_OVERVIEW, episode.overview)
        putInt(ARG_SEASON, episode.season)
        putInt(ARG_NUMBER, episode.number)
      }
      return EpisodeDetailsBottomSheet().apply { arguments = bundle }
    }
  }

  private val episodeTitle by lazy { arguments!!.getString(ARG_TITLE, "") }
  private val episodeOverview by lazy { arguments!!.getString(ARG_OVERVIEW, "") }
  private val episodeNumber by lazy { arguments!!.getInt(ARG_NUMBER) }
  private val episodeSeason by lazy { arguments!!.getInt(ARG_SEASON) }

  override fun getTheme(): Int = R.style.BottomSheetDialogTheme

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.view_episode_details, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.run {
      episodeDetailsName.text = context.getString(R.string.textSeasonEpisode, episodeSeason, episodeNumber)
      episodeDetailsTitle.text = episodeTitle
      episodeDetailsOverview.text = episodeOverview
    }
  }


}