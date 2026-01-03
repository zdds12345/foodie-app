package com.zhengdesheng.z202304100318.ademo.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhengdesheng.z202304100318.ademo.data.model.RecipeData
import com.zhengdesheng.z202304100318.ademo.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onItemClick: (RecipeData) -> Unit
) : ListAdapter<RecipeData, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
        private val onItemClick: (RecipeData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: RecipeData) {
            binding.apply {
                textViewRecipeTitle.text = recipe.title
                textViewTime.text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes}分钟"
                textViewServings.text = "${recipe.servings}人份"
                textViewDifficulty.text = recipe.difficulty

                // 显示前3个食材
                val ingredientsText = recipe.ingredients.take(3).joinToString(separator = "、")
                textViewIngredients.text = "食材：$ingredientsText"

                // 使用tag来确保图片正确对应
                imageViewRecipe.tag = recipe.imageUrl

                if (recipe.imageUrl.isNotEmpty()) {
                    Glide.with(imageViewRecipe.context)
                        .load(recipe.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .dontAnimate()
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                            override fun onResourceReady(resource: android.graphics.drawable.Drawable, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) {
                                // 只有当tag匹配时才设置图片
                                if (imageViewRecipe.tag == recipe.imageUrl) {
                                    imageViewRecipe.setImageDrawable(resource)
                                }
                            }

                            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                                imageViewRecipe.setImageDrawable(placeholder)
                            }
                        })
                } else {
                    imageViewRecipe.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                root.setOnClickListener {
                    onItemClick(recipe)
                }
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeData>() {
        override fun areItemsTheSame(oldItem: RecipeData, newItem: RecipeData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeData, newItem: RecipeData): Boolean {
            return oldItem == newItem
        }
    }
}