package com.example.android.mediacontroller.tests

import android.text.Editable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mediacontroller.R
import com.example.android.mediacontroller.TestOptionDetails
import com.example.android.mediacontroller.TestResult
import com.example.android.mediacontroller.tests.MediaAppTestManager.Companion.NO_TEST_ID

/**
 * RecyclerView Adapter for list of [TestOptionDetails].
 */
internal class TestOptionsAdapter(private val callback: TestOptionsAdapterCallback) :
  ListAdapter<TestOptionDetails, TestOptionViewHolder>(TestOptionDiff()) {

  // Workaround for storing current state in custom model.
  private var runningTestId: Int = NO_TEST_ID

  // Workaround for storing query in custom model.
  private val testIdToQueryText = SparseArray<String>() // TODO: save state in VM

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TestOptionViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.media_test_option_fork, parent, false)
  ).apply {
    cardButton.setOnClickListener {
      callback.onTestClick(test, testIdToQueryText.get(test.id, ""))
    }
    resultsButton.setOnClickListener {
      callback.onResultsClick(test)
    }
    queryEditText.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        testIdToQueryText.put(test.id, s.toString())
      }

      override fun afterTextChanged(s: Editable?) {}

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    })
  }

  override fun onBindViewHolder(holder: TestOptionViewHolder, position: Int) {
    val test = getItem(position)
    holder.test = test
    holder.cardView.setCardBackgroundColor(ResourcesCompat.getColor(
      holder.itemView.resources,
      determineColorResIdForResult(test.testResult),
      holder.itemView.context.theme
    ))
    holder.headerTextView.text = test.name
    holder.descriptionTextView.text = test.desc
    // TODO: Store on custom model
    if (test.id == 0) {
      holder.queryEditText.visibility = View.GONE
    } else {
      holder.queryEditText.visibility = View.VISIBLE
      holder.queryEditText.setText(testIdToQueryText.get(test.id, ""))
    }
    holder.cardButton.isEnabled = runningTestId == NO_TEST_ID
    holder.cardButton.visibility = if (isTestRunning(test.id)) View.INVISIBLE else View.VISIBLE
    holder.resultsButton.visibility =
      if (isTestRunning(test.id) || test.testResult != TestResult.NONE) View.VISIBLE else View.GONE
    holder.progressBar.visibility = if (isTestRunning(test.id)) View.VISIBLE else View.GONE
  }

  private fun determineColorResIdForResult(testResult: TestResult) =
    when (testResult) {
      TestResult.FAIL -> R.color.test_result_fail
      TestResult.PASS -> R.color.test_result_pass
      TestResult.OPTIONAL_FAIL -> R.color.test_result_optional_fail
      else -> android.R.color.white
    }

  private fun isTestRunning(testId: Int) = runningTestId == testId

  /**
   * Update the state of the tests.
   */
  fun submitRunningTestId(runningTestId: Int = NO_TEST_ID) {
    this.runningTestId = runningTestId
    notifyItemRangeChanged(0, itemCount) // TODO: remove view blink with payloads
  }

}

/**
 * Holds views for displaying [TestOptionDetails].
 */
internal class TestOptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val cardView: CardView = view.findViewById(R.id.card_view)
  val headerTextView: TextView = view.findViewById(R.id.card_header)
  val descriptionTextView: TextView = view.findViewById(R.id.card_text)
  val queryEditText: EditText = view.findViewById(R.id.query_edit_text)
  val cardButton: Button = view.findViewById(R.id.card_button)
  val resultsButton: Button = view.findViewById(R.id.results_button)
  val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
  lateinit var test: TestOptionDetails
}

/**
 * Determine if two items are the same. Update contents for TestResult.
 */
private class TestOptionDiff : DiffUtil.ItemCallback<TestOptionDetails>() {
  override fun areItemsTheSame(oldItem: TestOptionDetails, newItem: TestOptionDetails) =
    oldItem.id == newItem.id

  /**
   * TestResult is the only value that will update on this model.
   */
  override fun areContentsTheSame(oldItem: TestOptionDetails, newItem: TestOptionDetails) =
    oldItem.testResult == newItem.testResult
}

/**
 * Pass individual button clicks back up the stack.
 */
internal interface TestOptionsAdapterCallback {
  fun onTestClick(test: TestOptionDetails, query: String)
  fun onResultsClick(test: TestOptionDetails)
}
