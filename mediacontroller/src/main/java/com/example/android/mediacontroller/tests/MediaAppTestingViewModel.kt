package com.example.android.mediacontroller.tests

import android.app.Application
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.mediacontroller.MediaAppDetails
import com.example.android.mediacontroller.TestOptionDetails

/**
 * Scope and hold dependencies for [MediaAppTestingActivityFork].
 */
internal class MediaAppTestingViewModel(private val testManager: MediaAppTestManager) : ViewModel() {

  val playbackState: LiveData<String> = testManager.playbackState
  val metadata: LiveData<String> = testManager.metadata
  val repeatMode: LiveData<String> = testManager.repeatMode
  val shuffleMode: LiveData<String> = testManager.shuffleMode
  val queueTitle: LiveData<String> = testManager.queueTitle
  val queue: LiveData<String> = testManager.queue
  val queueItems: LiveData<List<MediaSessionCompat.QueueItem>> = testManager.queueItems

  val tests: LiveData<List<TestOptionDetails>> = testManager.tests
  val runningTestId: LiveData<Int> = testManager.runningTestId

  fun onTestClick(test: TestOptionDetails, query: String) {
    testManager.runTest(test, query)
  }

  override fun onCleared() {
    testManager.onDestroy()
  }
}

/**
 * Provide instance of [MediaAppTestingViewModel].
 */
internal class MediaAppTestingViewModelFactory(
  private val application: Application,
  private val appDetails: MediaAppDetails
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>): T =
    MediaAppTestingViewModel(MediaAppTestManager.instance(application, appDetails)) as T
}
