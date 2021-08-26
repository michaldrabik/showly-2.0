package com.michaldrabik.ui_news

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.michaldrabik.ui_news.recycler.NewsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.view_news_filters.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsFragment :
  BaseFragment<NewsViewModel>(R.layout.fragment_news),
  OnTabReselectedListener {

  companion object {
    private const val ARG_HEADER_POSITION = "ARG_HEADER_POSITION"
  }

  override val viewModel by viewModels<NewsViewModel>()

  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.newsSwipeRefreshEndOffset) }

  private var tabsService: ServiceConnection? = null
  private var tabsClient: CustomTabsClient? = null
  private var adapter: NewsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  private var headerTranslation = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
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
    setupSwipeRefresh()
    setupCustomTabs()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
        }
      }
    }
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
    with(fragmentNewsFiltersView) {
      onChipsChangeListener = { viewModel.loadItems(false, it) }
      translationY = headerTranslation
    }
  }

  private fun setupStatusBar() {
    fragmentNewsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      fragmentNewsRecycler.updatePadding(top = dimenToPx(R.dimen.newsRecyclerTopPadding) + statusBarSize)
      fragmentNewsHeaderView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      fragmentNewsFiltersView.updateTopMargin(dimenToPx(R.dimen.newsFiltersTopPadding) + statusBarSize)
      fragmentNewsSwipeRefresh.setProgressViewOffset(true, 0, swipeRefreshEndOffset + statusBarSize)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = NewsAdapter(
      itemClickListener = { openLink(it.item) },
      listChangeListener = { scrollToTop(false) }
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

  private fun setupSwipeRefresh() =
    with(fragmentNewsSwipeRefresh) {
      val color = requireContext().colorFromAttr(R.attr.colorAccent)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setOnRefreshListener {
        viewModel.loadItems(forceRefresh = true)
      }
    }

  private fun setupCustomTabs() {
    tabsService = object : CustomTabsServiceConnection() {
      override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        tabsClient = client
        tabsClient?.warmup(0)
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        tabsClient = null
      }
    }
    CustomTabsClient.bindCustomTabsService(
      requireActivity(),
      "com.android.chrome",
      (tabsService as CustomTabsServiceConnection)
    )
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      isEnabled = false
      activity?.onBackPressed()
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

      val closeButton = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> R.drawable.ic_arrow_back_tabs
        Configuration.UI_MODE_NIGHT_NO -> R.drawable.ic_arrow_back_tabs_black
        else -> R.drawable.ic_arrow_back_tabs
      }

      val tabsIntent = CustomTabsIntent.Builder()
        .setCloseButtonIcon(BitmapFactory.decodeResource(resources, closeButton))
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

  private fun render(ui: NewsUiState) {
    with(ui) {
      adapter?.setItems(items)
      fragmentNewsRecycler.fadeIf(items.isNotEmpty())
      fragmentNewsFiltersView.fadeIf(items.isNotEmpty())
      fragmentNewsEmptyView.fadeIf(items.isEmpty() && !isLoading)

      fragmentNewsSwipeRefresh.isRefreshing = isLoading
      fragmentNewsFiltersView.isEnabled = !isLoading
    }
  }

  private fun scrollToTop(smooth: Boolean = true) {
    fragmentNewsHeaderView.animate().translationY(0F).start()
    fragmentNewsFiltersView.animate().translationY(0F).start()
    when {
      smooth -> fragmentNewsRecycler.smoothScrollToPosition(0)
      else -> fragmentNewsRecycler.scrollToPosition(0)
    }
  }

  override fun onTabReselected() = scrollToTop()

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    tabsClient = null
    if (tabsService != null) {
      activity?.unbindService(tabsService!!)
      tabsService = null
    }
    super.onDestroyView()
  }
}
