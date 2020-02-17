package com.example.android.mediacontroller.tests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mediacontroller.MediaAppDetails
import com.example.android.mediacontroller.R

class MediaAppTestResultsFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_media_app_test_results, container, false)

    val testId = getTestIdFromArguments(arguments)
    val appDetails = getAppDetailsFromArguments(arguments)

    val viewModel = ViewModelProvider(
      this,
      ResultsViewModelFactory(testId, requireActivity().application, appDetails)
    ).get(ResultsViewModel::class.java)

    val resultsAdapter = ResultsAdapter()
    viewModel.results.observe(this, Observer(resultsAdapter::submitList))
    val resultsRecyclerView: RecyclerView = view.findViewById(R.id.results_recycler_view)
    resultsRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = resultsAdapter
    }

    return view
  }

  companion object {

    /**
     * Create an instance of [MediaAppTestResultsFragment].
     */
    fun create(testId: Int, appDetails: MediaAppDetails) = MediaAppTestResultsFragment().apply {
      arguments = Bundle().apply {
        putInt(TEST_ID_KEY, testId)
        putParcelable(APP_DETAILS_KEY, appDetails)
      }
    }

    /**
     * Retrieve TestId from arguments.
     */
    private fun getTestIdFromArguments(arguments: Bundle?) = arguments?.getInt(TEST_ID_KEY)
      ?: throw IllegalArgumentException()

    /**
     * Retrieve MediaAppDetails from arguments.
     */
    private fun getAppDetailsFromArguments(arguments: Bundle?) =
      arguments?.getParcelable<MediaAppDetails>(APP_DETAILS_KEY)
        ?: throw IllegalArgumentException()

    private const val TEST_ID_KEY = "TEST_ID_KEY"
    private const val APP_DETAILS_KEY = "APP_DETAILS_KEY"

  }

}

/**
 * RecyclerView adapter for displaying test results.
 */
private class ResultsAdapter : ListAdapter<String, ResultViewHolder>(ResultDiff()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ResultViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.media_app_test_result, parent, false)
  )

  override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
    holder.resultTextView.text = getItem(position)
  }

}

/**
 * Holds views for displaying test results.
 */
private class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val resultTextView: TextView = view.findViewById(R.id.result_text_view)
}

/**
 * Basic String comparison Diff.
 */
private class ResultDiff : DiffUtil.ItemCallback<String>() {
  override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
  override fun areContentsTheSame(oldItem: String, newItem: String) = true
}
