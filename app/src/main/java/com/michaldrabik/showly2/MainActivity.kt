package com.michaldrabik.showly2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
      if (currentDestination?.id != R.id.watchlistFragment) {
        bottomNavigationView.selectedItemId = R.id.menuWatchlist
        return
      }
      super.onBackPressed()
    }
  }
}
