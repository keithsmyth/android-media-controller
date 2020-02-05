package com.example.android.mediacontroller

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mediacontroller.tasks.FindMediaAppsTask
import com.example.android.mediacontroller.tasks.FindMediaBrowserAppsTask

class LaunchActivityFork : AppCompatActivity() {

  private lateinit var filterLayout: ViewGroup

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_launch_fork)
    setSupportActionBar(findViewById(R.id.toolbar))

    // TODO: Filter View
    filterLayout = findViewById(R.id.filter_layout)

    val viewModel = ViewModelProvider(this).get(LaunchViewModel::class.java)

    val mediaAppsAdapter = MediaAppsAdapter(object : MediaAppSelectedListener {
      override fun onMediaAppClicked(mediaAppDetails: MediaAppDetails, isTest: Boolean) {
        startActivity(
          if (isTest) MediaAppTestingActivity.buildIntent(this@LaunchActivityFork, mediaAppDetails)
          else MediaAppControllerActivity.buildIntent(this@LaunchActivityFork, mediaAppDetails)
        )
      }
    })

    viewModel.mediaApps.observe(this, Observer(mediaAppsAdapter::submitItems))

    val mediaAppsRecyclerView: RecyclerView = findViewById(R.id.app_list)
    mediaAppsRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = mediaAppsAdapter
    }

    // TODO: handleIntentExtras, onNewIntent

  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.launch, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == R.id.filter) {
      toggleFilterMenu()
      true
    } else super.onOptionsItemSelected(item)
  }

  private fun toggleFilterMenu() {
    filterLayout.visibility = if (filterLayout.visibility == View.GONE) View.VISIBLE else View.GONE
  }

}

/**
 * Scope models for view.
 */
internal class LaunchViewModel(app: Application) : AndroidViewModel(app) {

  // TODO: Session listener

  val mediaApps: LiveData<List<MediaAppDetails>> = MutableLiveData<List<MediaAppDetails>>().apply {
    FindMediaBrowserAppsTask(app, object : FindMediaAppsTask.AppListUpdatedCallback {
      override fun onAppListUpdated(mediaAppEntries: List<MediaAppDetails>) {
        // TODO: Auto, Automotive
        postValue(mediaAppEntries)
      }
    }).execute()
  }

  // TODO: Handle refresh

}

/**
 * Click listener for when an app is selected.
 */
private interface MediaAppSelectedListener {
  fun onMediaAppClicked(mediaAppDetails: MediaAppDetails, isTest: Boolean)
}

/**
 * Display list of media apps.
 */
private class MediaAppsAdapter(
  private val mediaAppSelectedListener: MediaAppSelectedListener
) : RecyclerView.Adapter<MediaAppViewHolder>() {

  private val items = mutableListOf<MediaAppDetails>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MediaAppViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.media_app_item_fork, parent, false)
  ).apply {
    controlButton.setOnClickListener {
      mediaAppSelectedListener.onMediaAppClicked(mediaAppDetails, isTest = false)
    }
    testButton.setOnClickListener {
      mediaAppSelectedListener.onMediaAppClicked(mediaAppDetails, isTest = true)
    }
  }

  override fun onBindViewHolder(holder: MediaAppViewHolder, position: Int) {
    val mediaApp = items[position]
    holder.appIconImageView.setImageBitmap(mediaApp.icon)
    holder.appNameTextView.text = mediaApp.appName
    holder.packageNameTextView.text = mediaApp.packageName
    holder.mediaAppDetails = mediaApp
  }

  override fun getItemCount() = items.size

  /**
   * Replace all current items.
   */
  fun submitItems(newItems: List<MediaAppDetails>) {
    items.clear()
    items.addAll(newItems)
    notifyDataSetChanged()
  }

}

/**
 * Hold views for single media app.
 */
private class MediaAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val appIconImageView: ImageView = view.findViewById(R.id.app_icon_image_view)
  val appNameTextView: TextView = view.findViewById(R.id.app_name_text_view)
  val packageNameTextView: TextView = view.findViewById(R.id.package_name_text_view)
  val mediaSessionImageView: ImageView = view.findViewById(R.id.media_session_image_view)
  val mediaBrowserImageView: ImageView = view.findViewById(R.id.media_browser_image_view)
  val autoImageView: ImageView = view.findViewById(R.id.auto_image_view)
  val automotiveImageView: ImageView = view.findViewById(R.id.automotive_image_view)
  val controlButton: Button = view.findViewById(R.id.app_control_button)
  val testButton: Button = view.findViewById(R.id.app_test_button)
  lateinit var mediaAppDetails: MediaAppDetails
}
