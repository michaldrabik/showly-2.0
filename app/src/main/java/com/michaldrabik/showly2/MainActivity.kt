package com.michaldrabik.showly2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.michaldrabik.network.Cloud
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  @Inject lateinit var cloud: Cloud

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
    setContentView(R.layout.activity_main)
  }
}
