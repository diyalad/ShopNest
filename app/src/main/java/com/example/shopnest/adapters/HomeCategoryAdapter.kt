package com.example.shopnest.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Category
import com.example.shopnest.R
import java.io.File

class HomeCategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder>() {

    // View holder class with improved image loading
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_icon)
        //private val categoryCount: TextView? = itemView.findViewById(R.id.category_icon) // Optional count display

        fun bind(category: Category) {
            // Set category name
            categoryName.text = category.name

            // Set click listener
            itemView.setOnClickListener { onItemClick(category) }

            // Load image from file path with better error handling
            loadCategoryImage(category.imagePath)

            // Optional: Display item count if available in layout
            //categoryCount?.text = category.itemCount?.toString() ?: ""
        }

        private fun loadCategoryImage(imagePath: String?) {
            try {
                if (!imagePath.isNullOrEmpty()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        categoryImage.setImageBitmap(bitmap)
                        return
                    }
                }
                setPlaceholderImage()
            } catch (e: Exception) {
                setPlaceholderImage()
            }
        }

        private fun setPlaceholderImage() {
            categoryImage.setImageResource(R.drawable.ic_category_placeholder)
        }
    }

    // Adapter overrides
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    // Update function for dynamic data changes
    fun updateCategories(newCategories: List<Category>) {
        (categories as? MutableList)?.let {
            it.clear()
            it.addAll(newCategories)
            notifyDataSetChanged()
        }
    }

    // Optional: Image loading callback interface
    interface OnImageLoadListener {
        fun onImageLoaded(position: Int, bitmap: Bitmap)
        fun onImageLoadFailed(position: Int)
    }
}