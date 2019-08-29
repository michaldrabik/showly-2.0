package com.michaldrabik.showly2

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.utilities.dimenToPx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val navigationHeight by lazy { dimenToPx(R.dimen.bottomNavigationHeightPadded) }
  private val decelerateInterpolator by lazy { DecelerateInterpolator(2F) }
  private val mainDestinations = arrayOf(R.id.watchlistFragment, R.id.discoverFragment)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
    setContentView(R.layout.activity_main)
    setupNavigation()
  }

  private fun setupNavigation() {
    bottomNavigationView.setOnNavigationItemSelectedListener { item ->
      if (bottomNavigationView.selectedItemId == item.itemId) {
        return@setOnNavigationItemSelectedListener true
      }

      val target = when (item.itemId) {
        R.id.menuWatchlist -> R.id.actionNavigateWatchlistFragment
        R.id.menuDiscover -> R.id.actionNavigateDiscoverFragment
        else -> throw IllegalStateException("Invalid menu item.")
      }

      navigationHost.findNavController().navigate(target)
      return@setOnNavigationItemSelectedListener true
    }
  }

  override fun onBackPressed() {
    navigationHost.findNavController().run {
      if (currentDestination?.id in mainDestinations) {
        bottomNavigationView.selectedItemId = R.id.menuWatchlist
        return
      }
      super.onBackPressed()
    }
    showNavigation()
  }

  fun hideNavigation() {
    bottomNavigationWrapper.animate()
      .translationYBy(navigationHeight.toFloat())
      .setDuration(400)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  fun showNavigation() {
    bottomNavigationWrapper.animate()
      .translationY(0F)
      .setDuration(400)
      .setInterpolator(decelerateInterpolator)
      .start()
  }
}
