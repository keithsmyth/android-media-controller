package com.example.android.mediacontroller.tests

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.mediacontroller.MediaAppDetails
import com.example.android.mediacontroller.R
import com.example.android.mediacontroller.TestOptionDetails
import com.example.android.mediacontroller.TestResult
import com.example.android.mediacontroller.formatMetadata
import com.example.android.mediacontroller.formatPlaybackState
import com.example.android.mediacontroller.repeatModeToName
import com.example.android.mediacontroller.shuffleModeToName
import java.util.*

/**
 * Wrapper around MediaBrowser and MediaController. Exposes formatted state and tests.
 */
class MediaAppTestManager(
  private val application: Application,
  private val appDetails: MediaAppDetails,
  private val testsProvider: MediaAppTestsProvider,
  private val mainHandler: Handler
) {
    private val testLogs = SparseArray<MutableLiveData<List<String>>>()

    val playbackState = MutableLiveData<String>()
    val metadata = MutableLiveData<String>()
    val repeatMode = MutableLiveData<String>()
    val shuffleMode = MutableLiveData<String>()
    val queueTitle = MutableLiveData<String>()
    val queue = MutableLiveData<String>()
    val queueItems = MutableLiveData<List<MediaSessionCompat.QueueItem>>()

    val tests = MutableLiveData<List<TestOptionDetails>>()
    // Workaround as test logger does not consider testId
    val runningTestId = MutableLiveData<Int>(NO_TEST_ID)

    private val browserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            initMediaController()
            refreshMediaControllerState()
            initTests()
        }

        override fun onConnectionSuspended() {
            // TODO: Error state
        }

        override fun onConnectionFailed() {
            // TODO: Error state
        }
    }

    private val mediaBrowser = MediaBrowserCompat(
            application,
            appDetails.componentName,
            browserConnectionCallback,
            null
    ).apply { connect() }

    private lateinit var mediaController: MediaControllerCompat

    private lateinit var controllerCallback: MediaControllerCompat.Callback

    private fun initMediaController() {
        val token = appDetails.sessionToken
                ?: if (mediaBrowser.isConnected) mediaBrowser.sessionToken
                else throw IllegalStateException()

        controllerCallback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                playbackState.value = formatPlaybackState(state)
            }

            override fun onMetadataChanged(md: MediaMetadataCompat?) {
                metadata.value = formatMetadata(md)
            }

            override fun onRepeatModeChanged(mode: Int) {
                repeatMode.value = repeatModeToName(mode)
            }

            override fun onShuffleModeChanged(mode: Int) {
                shuffleMode.value = shuffleModeToName(mode)
            }

            override fun onQueueTitleChanged(title: CharSequence?) {
                queueTitle.value = title.toString()
            }

            override fun onQueueChanged(q: MutableList<MediaSessionCompat.QueueItem>?) {
                queue.value = application.getString(R.string.queue_size, q?.size ?: 0)
                queueItems.value = q ?: emptyList()
            }
        }

        mediaController = MediaControllerCompat(application, token).apply {
            registerCallback(controllerCallback)
        }
    }

    private fun initTests() {
        val allTests = testsProvider
          .provideBasicTests(mediaController, ::logTestStep)
          .toMutableList()
        if (appDetails.supportsAuto || appDetails.supportsAutomotive) {
            allTests +=
              testsProvider.provideCommonTests(mediaBrowser, mediaController, ::logTestStep)
        }
        if (appDetails.supportsAutomotive) {
            allTests +=
              testsProvider.provideAutomotiveTests(mediaBrowser, mediaController, ::logTestStep)
        }
        tests.value = allTests
    }

    /**
     * Immediately update media controller state.
     */
    fun refreshMediaControllerState() {
        controllerCallback.run {
            onPlaybackStateChanged(mediaController.playbackState)
            onMetadataChanged(mediaController.metadata)
            onRepeatModeChanged(mediaController.repeatMode)
            onShuffleModeChanged(mediaController.shuffleMode)
            onQueueTitleChanged(mediaController.queueTitle)
            onQueueChanged(mediaController.queue)
        }
    }

    /**
     * Instruct the Manager to run the test.
     */
    fun runTest(test: TestOptionDetails, query: String = "") {
        testLogs.put(test.id, MutableLiveData(listOf()))
        runningTestId.value = test.id
        test.runTest(query, ::onTestComplete, test.id)
    }

    fun getTestLogs(testId: Int): LiveData<List<String>> {
        return testLogs.get(testId) ?: throw IllegalArgumentException()
    }

    private fun logTestStep(tag: String, message: String) {
        mainHandler.post {
            val testId = runningTestId.value
            if (testId == NO_TEST_ID || testId == null) throw IllegalStateException()

            val update = "[${Date()}] <$tag>:\n$message"

            val currentLogs = testLogs.get(testId, MutableLiveData(mutableListOf()))
            currentLogs.value = (currentLogs.value?.toMutableList() ?: mutableListOf()).apply {
                add(0, update)
            }
            testLogs.put(testId, currentLogs)
        }
    }

    private fun onTestComplete(result: TestResult, testId: Int) {
        mainHandler.post {
            runningTestId.value = NO_TEST_ID
            val allTests = tests.value?.toMutableList()
            val test = allTests?.get(testId) ?: throw IllegalArgumentException()
            allTests[testId] = test.copy(testResult = result)
            tests.value = allTests
        }
    }

    /**
     * Syncs up with ViewModel onCleared.
     */
    fun onDestroy() {
        if (::mediaController.isInitialized) {
            mediaController.unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()
    }

    companion object {

        fun instance(application: Application, appDetails: MediaAppDetails): MediaAppTestManager {
            var thisTestManager = testManager
            if (thisTestManager == null) {
                synchronized(this) {
                    thisTestManager = testManager ?: create(application, appDetails)
                    testManager = thisTestManager
                }
            }
            return thisTestManager!!
        }

        private fun create(application: Application, appDetails: MediaAppDetails) =
          MediaAppTestManager(
            application,
            appDetails,
            MediaAppTestsProvider(application, appDetails, application.packageManager),
            Handler(Looper.getMainLooper())
          )

        private var testManager: MediaAppTestManager? = null

        const val NO_TEST_ID = -1
    }

}
