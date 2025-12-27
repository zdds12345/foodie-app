package com.zhengdesheng.z202304100318.ademo.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import com.zhengdesheng.z202304100318.ademo.databinding.ItemDiaryBinding
import java.text.SimpleDateFormat
import java.util.*

class DiaryAdapter(
    private val onItemClick: (Diary) -> Unit,
    private val onItemLongClick: (Diary) -> Unit
) : ListAdapter<Diary, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaryViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiaryViewHolder(
        private val binding: ItemDiaryBinding,
        private val onItemClick: (Diary) -> Unit,
        private val onItemLongClick: (Diary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(diary: Diary) {
            binding.apply {
                textViewTitle.text = diary.title
                textViewContent.text = diary.content
                textViewRating.text = diary.rating.toString()
                ratingBar.rating = diary.rating
                textViewTags.text = diary.tags

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                textViewDate.text = dateFormat.format(Date(diary.createdAt))

                root.setOnClickListener {
                    onItemClick(diary)
                }

                root.setOnLongClickListener {
                    onItemLongClick(diary)
                    true
                }
            }
        }
    }

    private class DiaryDiffCallback : DiffUtil.ItemCallback<Diary>() {
        override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem == newItem
        }
    }
}
