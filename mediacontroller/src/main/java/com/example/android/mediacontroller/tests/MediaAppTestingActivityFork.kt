package com.example.android.mediacontroller.tests

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.android.mediacontroller.BitmapUtils
import com.example.android.mediacontroller.MediaAppDetails
import com.example.android.mediacontroller.R
import com.example.android.mediacontroller.Test
import com.example.android.mediacontroller.TestOptionDetails
import com.google.android.material.tabs.TabLayout

/**
 * Media App Testing Activity.
 */
class MediaAppTestingActivityFork : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_app_testing_fork)

        Test.androidResources = application.resources // TODO: Fix this pattern

        val appDetails = getAppDetails(intent)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setIcon(BitmapDrawable(
                    resources,
              BitmapUtils.createToolbarIcon(resources, appDetails.icon)
            ))
            title = appDetails.appName
        }

        val pages: Array<View> = arrayOf(
                findViewById(R.id.media_controller_info_page),
                findViewById(R.id.media_controller_test_page)
        )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = object : PagerAdapter() {
            override fun getCount() = pages.size
            override fun isViewFromObject(view: View, item: Any) = view == item
            override fun instantiateItem(container: ViewGroup, position: Int) = pages[position]
        }
        val pageIndicator: TabLayout = findViewById(R.id.page_indicator)
        pageIndicator.setupWithViewPager(viewPager)

        val viewModel = ViewModelProvider(
          this,
          MediaAppTestingViewModelFactory(application, appDetails)
        ).get(MediaAppTestingViewModel::class.java)

        val playbackStateTextView: TextView = findViewById(R.id.playback_state_text)
        viewModel.playbackState.observe(this, Observer(playbackStateTextView::setText))

        val metadataTextView: TextView = findViewById(R.id.metadata_text)
        viewModel.metadata.observe(this, Observer(metadataTextView::setText))

        val repeatModeTextView: TextView = findViewById(R.id.repeat_mode_text)
        viewModel.repeatMode.observe(this, Observer(repeatModeTextView::setText))

        val shuffleModeTextView: TextView = findViewById(R.id.shuffle_mode_text)
        viewModel.shuffleMode.observe(this, Observer(shuffleModeTextView::setText))

        val queueTitleTextView: TextView = findViewById(R.id.queue_title_text)
        viewModel.queueTitle.observe(this, Observer(queueTitleTextView::setText))

        val queueTextView: TextView = findViewById(R.id.queue_text)
        viewModel.queue.observe(this, Observer(queueTextView::setText))

        val queueItemsAdapter = QueueItemsAdapter()
        viewModel.queueItems.observe(this, Observer(queueItemsAdapter::submitList))
        val queueRecyclerView: RecyclerView = findViewById(R.id.queue_item_list)
        queueRecyclerView.layoutManager = LinearLayoutManager(this)
        queueRecyclerView.adapter = queueItemsAdapter

        val testOptionsAdapter = TestOptionsAdapter(object : TestOptionsAdapterCallback {
            override fun onTestClick(test: TestOptionDetails, query: String) {
                viewModel.onTestClick(test, query)
            }

            override fun onResultsClick(test: TestOptionDetails) {
                val fragment = MediaAppTestResultsFragment.create(test.id, appDetails)
                supportFragmentManager.beginTransaction()
                  .add(R.id.fragment_container_view, fragment)
                  .addToBackStack(null)
                  .commit()
            }
        })
        viewModel.tests.observe(this, Observer(testOptionsAdapter::submitList))
        viewModel.runningTestId.observe(this, Observer(testOptionsAdapter::submitRunningTestId))
        val testOptionsRecyclerView: RecyclerView = findViewById(R.id.test_options_recycler_view)
        testOptionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = testOptionsAdapter
        }
    }

    companion object {

        /**
         * Builds an [Intent] to launch this Activity with a set of extras.
         */
        fun buildIntent(context: Context, appDetails: MediaAppDetails) =
                Intent(context, MediaAppTestingActivityFork::class.java).apply {
                    putExtra(APP_DETAILS_EXTRA, appDetails)
                }

        /**
         * Retrieve AppDetails extra from Intent.
         */
        private fun getAppDetails(intent: Intent?) =
                intent?.getParcelableExtra<MediaAppDetails>(APP_DETAILS_EXTRA)
                        ?: throw IllegalArgumentException()

        private const val APP_DETAILS_EXTRA =
                "com.example.android.mediacontroller.APP_DETAILS_EXTRA"


    }

}
