package com.example.android.mediacontroller.tests

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.mediacontroller.MediaAppDetails

/**
 * Scope and hold dependencies for [MediaAppTestResultsFragment].
 */
internal class ResultsViewModel(testId: Int, testManager: MediaAppTestManager) : ViewModel() {

  val results: LiveData<List<String>> = testManager.getTestLogs(testId)

}

/**
 * Provide instance of [ResultsViewModel].
 */
internal class ResultsViewModelFactory(
  private val testId: Int,
  private val application: Application,
  private val appDetails: MediaAppDetails
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>) =
    ResultsViewModel(testId, MediaAppTestManager.instance(application, appDetails)) as T
}
