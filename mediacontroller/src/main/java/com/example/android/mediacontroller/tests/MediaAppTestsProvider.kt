package com.example.android.mediacontroller.tests

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.android.mediacontroller.MediaAppDetails
import com.example.android.mediacontroller.R
import com.example.android.mediacontroller.TestOptionDetails
import com.example.android.mediacontroller.TestResult
import com.example.android.mediacontroller.runBrowseTreeDepthTest
import com.example.android.mediacontroller.runBrowseTreeStructureTest
import com.example.android.mediacontroller.runContentStyleTest
import com.example.android.mediacontroller.runCustomActionIconTypeTest
import com.example.android.mediacontroller.runErrorResolutionDataTest
import com.example.android.mediacontroller.runInitialPlaybackStateTest
import com.example.android.mediacontroller.runLauncherTest
import com.example.android.mediacontroller.runMediaArtworkTest
import com.example.android.mediacontroller.runPauseTest
import com.example.android.mediacontroller.runPlayFromMediaIdTest
import com.example.android.mediacontroller.runPlayFromSearchTest
import com.example.android.mediacontroller.runPlayFromUriTest
import com.example.android.mediacontroller.runPlayTest
import com.example.android.mediacontroller.runPreferenceTest
import com.example.android.mediacontroller.runSearchTest
import com.example.android.mediacontroller.runSeekToTest
import com.example.android.mediacontroller.runSkipToItemTest
import com.example.android.mediacontroller.runSkipToNextTest
import com.example.android.mediacontroller.runSkipToPrevTest
import com.example.android.mediacontroller.runStopTest

/**
 * Provides and provides dependencies for categorised tests.
 */
class MediaAppTestsProvider(
  private val application: Application,
  private val mediaAppDetails: MediaAppDetails,
  private val packageManager: PackageManager
) {

  fun provideBasicTests(
    controller: MediaControllerCompat,
    logger: (tag: String, message: String) -> Unit?
  ): List<TestOptionDetails> {
    /**
     * Tests the play() transport control. The test can start in any state, might enter a
     * transition state, but must eventually end in STATE_PLAYING. The test will fail for
     * any terminal state other than the starting state and STATE_PLAYING. The test
     * will also fail if the metadata changes unless the test began with null metadata.
     */
    val playTest = TestOptionDetails(
      0,
      getString(R.string.play_test_title),
      getString(R.string.play_test_desc),
      TestResult.NONE
    ) { _, callback, testId -> runPlayTest(testId, controller, callback, logger) }

    /**
     * Tests the playFromSearch() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with playback
     * position at 0. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. This test does not perform any metadata checks.
     */
    val playFromSearch = TestOptionDetails(
      1,
      getString(R.string.play_search_test_title),
      getString(R.string.play_search_test_desc),
      TestResult.NONE
    ) { query, callback, testId ->
      runPlayFromSearchTest(
        testId, query, controller, callback, logger)
    }

    /**
     * Tests the playFromMediaId() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with playback
     * position at 0. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. The test will also fail if query is empty/null. This test does not
     * perform any metadata checks.
     */
    val playFromMediaId = TestOptionDetails(
      2,
      getString(R.string.play_media_id_test_title),
      getString(R.string.play_media_id_test_desc),
      TestResult.NONE
    ) { query, callback, testId ->
      runPlayFromMediaIdTest(
        testId, query, controller, callback, logger)
    }

    /**
     * Tests the playFromUri() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with playback
     * position at 0. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. The test will also fail if query is empty/null. This test does not
     * perform any metadata checks.
     */
    val playFromUri = TestOptionDetails(
      3,
      getString(R.string.play_uri_test_title),
      getString(R.string.play_uri_test_desc),
      TestResult.NONE
    ) { query, callback, testId ->
      runPlayFromUriTest(
        testId, query, controller, callback, logger)
    }

    /**
     * Tests the pause() transport control. The test can start in any state, but must end in
     * STATE_PAUSED (but STATE_STOPPED is also okay if that is the state the test started with).
     * The test will fail for any terminal state other than the starting state, STATE_PAUSED,
     * and STATE_STOPPED. The test will also fail if the metadata changes unless the test began
     * with null metadata.
     */
    val pauseTest = TestOptionDetails(
      4,
      getString(R.string.pause_test_title),
      getString(R.string.pause_test_desc),
      TestResult.NONE
    ) { _, callback, testId -> runPauseTest(testId, controller, callback, logger) }

    /**
     * Tests the stop() transport control. The test can start in any state, but must end in
     * STATE_STOPPED or STATE_NONE. The test will fail for any terminal state other than the
     * starting state, STATE_STOPPED, and STATE_NONE. The test will also fail if the metadata
     * changes to a non-null media item different from the original media item.
     */
    val stopTest = TestOptionDetails(
      5,
      getString(R.string.stop_test_title),
      getString(R.string.stop_test_desc),
      TestResult.NONE
    ) { _, callback, testId -> runStopTest(testId, controller, callback, logger) }

    /**
     * Tests the skipToNext() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with the playback
     * position at 0 if a new media item is started or in the starting state if the media item
     * doesn't change. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. The metadata must change, but might just "change" to be the same as
     * the original metadata (e.g. if the next media item is the same as the current one); the
     * test will not pass if the metadata doesn't get updated at some point.
     */
    val skipToNextTest = TestOptionDetails(
      6,
      getString(R.string.skip_next_test_title),
      getString(R.string.skip_next_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runSkipToNextTest(
        testId, controller, callback, logger)
    }

    /**
     * Tests the skipToPrevious() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with the playback
     * position at 0 if a new media item is started or in the starting state if the media item
     * doesn't change. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. The metadata must change, but might just "change" to be the same as
     * the original metadata (e.g. if the previous media item is the same as the current one);
     * the test will not pass if the metadata doesn't get updated at some point.
     */
    val skipToPrevTest = TestOptionDetails(
      7,
      getString(R.string.skip_prev_test_title),
      getString(R.string.skip_prev_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runSkipToPrevTest(
        testId, controller, callback, logger)
    }

    /**
     * Tests the skipToQueueItem() transport control. The test can start in any state, might
     * enter a transition state, but must eventually end in STATE_PLAYING with the playback
     * position at 0 if a new media item is started or in the starting state if the media item
     * doesn't change. The test will fail for any terminal state other than the starting state
     * and STATE_PLAYING. The metadata must change, but might just "change" to be the same as
     * the original metadata (e.g. if the next media item is the same as the current one); the
     * test will not pass if the metadata doesn't get updated at some point.
     */
    val skipToItemTest = TestOptionDetails(
      8,
      getString(R.string.skip_item_test_title),
      getString(R.string.skip_item_test_desc),
      TestResult.NONE
    ) { query, callback, testId ->
      runSkipToItemTest(
        testId, query, controller, callback, logger)
    }

    /**
     * Tests the seekTo() transport control. The test can start in any state, might enter a
     * transition state, but must eventually end in a terminal state with playback position at
     * the requested timestamp. While not required, it is expected that the test will end in
     * the same state as it started. Metadata might change for this test if the requested
     * timestamp is outside the bounds of the current media item. The query should either be
     * a position in seconds or a change in position (number of seconds prepended by '+' to go
     * forward or '-' to go backwards). The test will fail if the query can't be parsed to a
     * Long.
     */
    val seekToTest = TestOptionDetails(
      9,
      getString(R.string.seek_test_title),
      getString(R.string.seek_test_desc),
      TestResult.NONE
    ) { query, callback, testId ->
      runSeekToTest(
        testId, query, controller, callback, logger)
    }

    return listOf(
      playTest,
      playFromSearch,
      playFromMediaId,
      playFromUri,
      pauseTest,
      stopTest,
      skipToNextTest,
      skipToPrevTest,
      skipToItemTest,
      seekToTest
    )
  }

  /**
   * Automotive and Auto shared tests
   */
  fun provideCommonTests(
    mediaBrowser: MediaBrowserCompat,
    controller: MediaControllerCompat,
    logger: (tag: String, message: String) -> Unit?
  ): List<TestOptionDetails> {
    val browseTreeDepthTest = TestOptionDetails(
      10,
      getString(R.string.browse_tree_depth_test_title),
      getString(R.string.browse_tree_depth_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        runBrowseTreeDepthTest(
          testId, controller, mediaBrowser, callback, logger)
      } else {
        Toast.makeText(
          application,
          "This test requires minSDK 24",
          Toast.LENGTH_SHORT)
          .show()
      }
    }

    val mediaArtworkTest = TestOptionDetails(
      11,
      getString(R.string.media_artwork_test_title),
      getString(R.string.media_artwork_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        runMediaArtworkTest(
          testId, controller, mediaBrowser, callback, logger)
      } else {
        Toast.makeText(
          application,
          "This test requires minSDK 24",
          Toast.LENGTH_SHORT)
          .show()
      }
    }

    val contentStyleTest = TestOptionDetails(
      12,
      getString(R.string.content_style_test_title),
      getString(R.string.content_style_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runContentStyleTest(
        testId, controller, mediaBrowser, callback, logger)
    }

    val customActionIconTypeTest = TestOptionDetails(
      13,
      getString(R.string.custom_actions_icon_test_title),
      getString(R.string.custom_actions_icon_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runCustomActionIconTypeTest(
        testId, application, controller, mediaAppDetails, callback,
        logger)
    }

    val supportsSearchTest = TestOptionDetails(
      14,
      getString(R.string.search_supported_test_title),
      getString(R.string.search_supported_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runSearchTest(
        testId, controller, mediaBrowser, callback, logger)
    }

    val initialPlaybackStateTest = TestOptionDetails(
      15,
      getString(R.string.playback_state_test_title),
      getString(R.string.playback_state_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runInitialPlaybackStateTest(
        testId, controller, callback, logger)
    }
    return listOf(
      browseTreeDepthTest,
      mediaArtworkTest,
      contentStyleTest,
      customActionIconTypeTest,
      supportsSearchTest,
      initialPlaybackStateTest
    )
  }

  /**
   * Automotive specific tests
   */
  fun provideAutomotiveTests(
    mediaBrowser: MediaBrowserCompat,
    controller: MediaControllerCompat,
    logger: (tag: String, message: String) -> Unit?
  ): List<TestOptionDetails> {
    val browseTreeStructureTest = TestOptionDetails(
      16,
      getString(R.string.browse_tree_structure_test_title),
      getString(R.string.browse_tree_structure_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        runBrowseTreeStructureTest(
          testId, controller, mediaBrowser, callback, logger)
      } else {
        Toast.makeText(
          application,
          getString(R.string.test_error_minsdk),
          Toast.LENGTH_SHORT)
          .show()
      }
    }

    val preferenceTest = TestOptionDetails(
      17,
      getString(R.string.preference_activity_test_title),
      getString(R.string.preference_activity_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runPreferenceTest(
        testId, controller, mediaAppDetails, packageManager, callback, logger)
    }

    val errorResolutionDataTest = TestOptionDetails(
      18,
      getString(R.string.error_resolution_test_title),
      getString(R.string.error_resolution_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runErrorResolutionDataTest(
        testId, controller, callback, logger)
    }

    val launcherTest = TestOptionDetails(
      19,
      getString(R.string.launcher_intent_test_title),
      getString(R.string.launcher_intent_test_desc),
      TestResult.NONE
    ) { _, callback, testId ->
      runLauncherTest(
        testId, controller, mediaAppDetails, packageManager, callback, logger)
    }
    return listOf(
      browseTreeStructureTest,
      preferenceTest,
      errorResolutionDataTest,
      launcherTest
    )
  }

  private fun getString(@StringRes resId: Int) = application.getString(resId)

}
