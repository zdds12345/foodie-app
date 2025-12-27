package com.zhengdesheng.z202304100318.ademo.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.databinding.ItemShopBinding

class ShopAdapter(
    private val onItemClick: (Shop) -> Unit
) : ListAdapter<Shop, ShopAdapter.ShopViewHolder>(ShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ShopViewHolder(
        private val binding: ItemShopBinding,
        private val onItemClick: (Shop) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
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

    private class ShopDiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem == newItem
        }
    }
}
