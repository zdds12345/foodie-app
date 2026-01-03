package com.zhengdesheng.z202304100318.ademo.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhengdesheng.z202304100318.ademo.data.model.ShopData
import com.zhengdesheng.z202304100318.ademo.databinding.ItemRecommendedShopBinding

class RecommendedShopAdapter(
    private val onItemClick: (ShopData) -> Unit
) : ListAdapter<ShopData, RecommendedShopAdapter.RecommendedShopViewHolder>(RecommendedShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedShopViewHolder {
        val binding = ItemRecommendedShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendedShopViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecommendedShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecommendedShopViewHolder(
        private val binding: ItemRecommendedShopBinding,
        private val onItemClick: (ShopData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: ShopData) {
            binding.apply {
                textViewShopName.text = shop.name
                textViewCategory.text = shop.category
                textViewAddress.text = shop.address
                textViewRating.text = shop.rating.toString()
                ratingBar.rating = shop.rating

                if (shop.imageUrl != null) {
                    Glide.with(imageViewShop.context)
                        .load(shop.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imageViewShop)
                }

                root.setOnClickListener {
                    onItemClick(shop)
                }
            }
        }
    }

    private class RecommendedShopDiffCallback : DiffUtil.ItemCallback<ShopData>() {
        override fun areItemsTheSame(oldItem: ShopData, newItem: ShopData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShopData, newItem: ShopData): Boolean {
            return oldItem == newItem
        }
    }
}
