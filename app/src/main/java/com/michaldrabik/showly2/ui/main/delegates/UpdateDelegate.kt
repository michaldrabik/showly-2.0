package com.michaldrabik.showly2.ui.main.delegates

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

interface UpdateDelegate {

  fun registerUpdate(
    activity: AppCompatActivity,
    onUpdateDownloaded: (AppUpdateManager) -> Unit
  )
}

class MainUpdateDelegate : UpdateDelegate, DefaultLifecycleObserver {

  companion object {
    private const val REQUEST_APP_UPDATE = 5278
    private const val DAYS_FOR_UPDATE = 3
  }

  private lateinit var activity: AppCompatActivity
  private lateinit var appUpdateManager: AppUpdateManager
  private lateinit var updateListener: InstallStateUpdatedListener

  private var onUpdateDownloaded: ((AppUpdateManager) -> Unit)? = null

  override fun registerUpdate(
    activity: AppCompatActivity,
    onUpdateDownloaded: (AppUpdateManager) -> Unit
  ) {
    this.activity = activity
    this.activity.lifecycle.addObserver(this)
    this.onUpdateDownloaded = onUpdateDownloaded
    this.appUpdateManager = AppUpdateManagerFactory.create(activity.applicationContext)
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    Timber.d("onCreate()")

    updateListener = InstallStateUpdatedListener {
      if (it.installStatus() == InstallStatus.DOWNLOADED) {
        onUpdateDownloaded?.invoke(appUpdateManager)
        if (this@MainUpdateDelegate::updateListener.isInitialized) {
          appUpdateManager.unregisterListener(updateListener)
        }
      }
    }

    appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
      Timber.d("Update info: $updateInfo")
      if (updateInfo.installStatus() == InstallStatus.DOWNLOADED) {
        onUpdateDownloaded?.invoke(appUpdateManager)
        return@addOnSuccessListener
      }

      if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        (updateInfo.clientVersionStalenessDays() ?: 0) >= DAYS_FOR_UPDATE &&
        updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
      ) {
        Timber.d("Starting update flow...")
        startUpdate(appUpdateManager, updateInfo)
      }
    }
  }

  override fun onDestroy(owner: LifecycleOwner) {
    appUpdateManager.unregisterListener(updateListener)
    onUpdateDownloaded = null
    Timber.d("onDestroy()")
    super.onDestroy(owner)
  }

  private fun startUpdate(
    appUpdateManager: AppUpdateManager,
    updateInfo: AppUpdateInfo,
  ) {
    appUpdateManager.registerListener(updateListener)
    appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.FLEXIBLE, activity, REQUEST_APP_UPDATE)
  }
}
