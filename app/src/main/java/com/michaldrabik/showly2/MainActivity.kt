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
        R.id.menuWatchlist -> R.id.watchlistFragment
        R.id.menuDiscover -> R.id.discoverFragment
        else -> throw IllegalStateException("Invalid menu item.")
      }

      navigationHost.findNavController().run {
        popBackStack(R.id.navigationHost, true)
        navigate(target)
      }

      return@setOnNavigationItemSelectedListener true
    }
  }
}
