package com.example.android.mediacontroller.tests

import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mediacontroller.R

/**
 * RecyclerView Adapter for list of [MediaSessionCompat.QueueItem].
 */
internal class QueueItemsAdapter :
  ListAdapter<MediaSessionCompat.QueueItem, QueueItemViewHolder>(QueueItemDiff()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = QueueItemViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.media_queue_item, parent, false)
  )

  override fun onBindViewHolder(holder: QueueItemViewHolder, position: Int) {
    val item = getItem(position)
    holder.queueIdTextView.text = item.queueId.toString()
    val description = item.description
    holder.titleTextView.text = description.title
    holder.subtitleTextView.text = description.subtitle
    holder.mediaIdTextView.text = description.mediaId
    holder.mediaUriTextView.text = description.mediaUri.toString()
  }

}

/**
 * Hold views for displaying QueueItem.
 */
internal class QueueItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val queueIdTextView: TextView = view.findViewById(R.id.queue_id)
  val titleTextView: TextView = view.findViewById(R.id.description_title)
  val subtitleTextView: TextView = view.findViewById(R.id.description_subtitle)
  val mediaIdTextView: TextView = view.findViewById(R.id.description_id)
  val mediaUriTextView: TextView = view.findViewById(R.id.description_uri)
}

/**
 * Determine if two items are the same.
 */
private class QueueItemDiff : DiffUtil.ItemCallback<MediaSessionCompat.QueueItem>() {

  override fun areItemsTheSame(
    oldItem: MediaSessionCompat.QueueItem,
    newItem: MediaSessionCompat.QueueItem
  ) = oldItem.description.mediaId == newItem.description.mediaId

  override fun areContentsTheSame(
    oldItem: MediaSessionCompat.QueueItem,
    newItem: MediaSessionCompat.QueueItem
  ) = true // Contents are not updated

}
