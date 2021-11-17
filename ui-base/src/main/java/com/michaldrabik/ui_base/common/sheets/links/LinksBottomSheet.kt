package com.michaldrabik.ui_base.common.sheets.links

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.view_links.*

@AndroidEntryPoint
class LinksBottomSheet : BaseBottomSheetFragment<LinksViewModel>() {

  @Parcelize
  data class Options(
    val ids: Ids,
    val title: String,
    val website: String,
    val type: Mode,
  ) : Parcelable

  companion object {
    fun createBundle(movie: Movie): Bundle {
      val options = Options(movie.ids, movie.title, movie.homepage, Mode.MOVIES)
      return bundleOf(NavigationArgs.ARG_OPTIONS to options)
    }

    fun createBundle(show: Show): Bundle {
      val options = Options(show.ids, show.title, show.homepage, Mode.SHOWS)
      return bundleOf(NavigationArgs.ARG_OPTIONS to options)
    }
  }

  override val layoutResId = R.layout.view_links

  private val options by lazy { (requireArguments().getParcelable<Options>(NavigationArgs.ARG_OPTIONS))!! }
  private val ids by lazy { options.ids }
  private val title by lazy { options.title }
  private val website by lazy { options.website }
  private val type by lazy { options.type }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[LinksViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
  }

  private fun setupView() {
    viewLinksButtonClose.onClick { dismiss() }
    setWebLink()
    setTraktLink()
    setTvdbLink()
    setTmdbLink()
    setImdbLink()
    viewLinksJustWatch.onClick {
      val country = viewModel.loadCountry()
      openWebUrl("https://www.justwatch.com/${country.code}/${country.justWatchQuery}?content_type=${type.type}&q=${Uri.encode(title)}")
    }
    viewLinksYouTube.onClick {
      openWebUrl("https://www.youtube.com/results?search_query=${getQuery()}")
    }
    viewLinksWiki.onClick {
      openWebUrl("https://en.wikipedia.org/w/index.php?search=${getQuery()}")
    }
    viewLinksGoogle.onClick {
      openWebUrl("https://www.google.com/search?q=${getQuery()}")
    }
    viewLinksDuckDuck.onClick {
      openWebUrl("https://duckduckgo.com/?q=${getQuery()}")
    }
    viewLinksGif.onClick {
      openWebUrl("https://giphy.com/search/${getQuery()}")
    }
    viewLinksTwitter.onClick {
      openWebUrl("https://twitter.com/search?q=${getQuery()}&src=typed_query")
    }
  }

  private fun setWebLink() {
    viewLinksWebsite.run {
      if (website.isBlank()) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick { openWebUrl(website) }
      }
    }
  }

  private fun setTraktLink() {
    viewLinksTrakt.run {
      if (ids.trakt.id == -1L) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick { openWebUrl("https://trakt.tv/search/trakt/${ids.trakt.id}?id_type=${type.type}") }
      }
    }
  }

  private fun setTvdbLink() {
    viewLinksTvdb.run {
      if (ids.tvdb.id == -1L) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick {
          when (type) {
            Mode.SHOWS -> openWebUrl("https://www.thetvdb.com/?id=${ids.tvdb.id}&tab=series")
            Mode.MOVIES -> openWebUrl("https://www.thetvdb.com/?id=${ids.tvdb.id}&tab=movies")
          }
        }
      }
    }
  }

  private fun setTmdbLink() {
    viewLinksTmdb.run {
      if (ids.tmdb.id == -1L) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick {
          when (type) {
            Mode.SHOWS -> openWebUrl("https://www.themoviedb.org/tv/${ids.tmdb.id}")
            Mode.MOVIES -> openWebUrl("https://www.themoviedb.org/movie/${ids.tmdb.id}")
          }
        }
      }
    }
  }

  private fun setImdbLink() {
    viewLinksImdb.run {
      if (ids.imdb.id.isBlank()) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick {
          val i = Intent(Intent.ACTION_VIEW)
          i.data = Uri.parse("imdb:///title/${ids.imdb.id}")
          try {
            startActivity(i)
          } catch (e: ActivityNotFoundException) {
            // IMDb App not installed. Start in web browser
            openWebUrl("https://www.imdb.com/title/${ids.imdb.id}")
          }
        }
      }
    }
  }

  private fun getQuery() = when (type) {
    Mode.SHOWS -> "$title TV Series"
    Mode.MOVIES -> "$title Movie"
  }
}
