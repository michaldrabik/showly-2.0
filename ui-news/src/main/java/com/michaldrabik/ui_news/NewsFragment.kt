package com.michaldrabik.ui_news

import android.content.ComponentName
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.di.UiNewsComponentProvider
import com.michaldrabik.ui_news.recycler.NewsAdapter
import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment :
  BaseFragment<NewsViewModel>(R.layout.fragment_news),
  OnTabReselectedListener {

  companion object {
    private const val ARG_HEADER_POSITION = "ARG_HEADER_POSITION"
  }

  override val viewModel by viewModels<NewsViewModel> { viewModelFactory }

  private var tabsClient: CustomTabsClient? = null
  private var adapter: NewsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  private var headerTranslation = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiNewsComponentProvider).provideNewsComponent().inject(this)
    super.onCreate(savedInstanceState)

    savedInstanceState?.let {
      headerTranslation = it.getFloat(ARG_HEADER_POSITION)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()
    setupCustomTabs()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
    }
  }

  private fun setupCustomTabs() {
    val serviceConnection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
      override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        tabsClient = client
        tabsClient?.warmup(0)
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        tabsClient = null
      }
    }
    CustomTabsClient.bindCustomTabsService(requireActivity(), "com.android.chrome", serviceConnection)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat(ARG_HEADER_POSITION, fragmentNewsHeaderView?.translationY ?: 0F)
  }

  override fun onPause() {
    enableUi()
    headerTranslation = fragmentNewsHeaderView.translationY
    super.onPause()
  }

  private fun setupView() {
    with(fragmentNewsHeaderView) {
      onSettingsClickListener = { openSettings() }
      translationY = headerTranslation
    }
  }

  private fun setupStatusBar() {
    fragmentNewsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      fragmentNewsRecycler.updatePadding(top = statusBarSize + dimenToPx(R.dimen.newsRecyclerTopPadding))
      fragmentNewsHeaderView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = NewsAdapter(
      itemClickListener = { openLink(it.item) }
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    fragmentNewsRecycler.apply {
      adapter = this@NewsFragment.adapter
      layoutManager = this@NewsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
      addDivider(R.drawable.divider_news, VERTICAL)
    }
  }

  private fun render(uiModel: NewsUiModel) {
    uiModel.run {
      items?.let {
        fragmentNewsRecycler.fadeIf(it.isNotEmpty())
        fragmentNewsEmptyView.fadeIf(it.isEmpty())
        adapter?.setItems(it)
      }
    }
  }

  private fun openLink(item: NewsItem) {
    if (item.isVideo) {
      openWebUrl(item.url) ?: showSnack(MessageEvent.info(R.string.errorCouldNotFindApp))
    } else {
      val context = requireActivity()
      val tabColor = context.colorFromAttr(R.attr.colorBottomMenuBackground)

      val params = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(tabColor)
        .setNavigationBarColor(tabColor)
        .build()

      val tabsIntent = CustomTabsIntent.Builder()
        .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back_tabs))
        .setDefaultColorSchemeParams(params)
        .setStartAnimations(context, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_from_right)
        .setExitAnimations(context, R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
        .build()

      tabsIntent.launchUrl(context, Uri.parse(item.url))
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionNewsFragmentToSettingsFragment)
  }

  private fun scrollToTop(smooth: Boolean = true) {
    fragmentNewsHeaderView.animate().translationY(0F).start()
    when {
      smooth -> fragmentNewsRecycler.smoothScrollToPosition(0)
      else -> fragmentNewsRecycler.scrollToPosition(0)
    }
  }

  override fun onTabReselected() = scrollToTop()

  override fun onDestroyView() {
    tabsClient = null
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
