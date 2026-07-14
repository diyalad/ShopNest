package com.example.shopnest.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Category
import com.example.shopnest.R
import java.io.File
import java.util.concurrent.Executors

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // Thread pool for background image loading
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    // Cache for loaded bitmaps
    private val imageCache = mutableMapOf<String, Bitmap>()

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        var currentImagePath: String? = null

        fun bind(category: Category) {
            categoryName.text = category.name
            currentImagePath = category.imagePath

            // Set placeholder initially
            categoryImage.setImageResource(R.drawable.ic_category_placeholder)

            // Load image if path exists
            if (category.imagePath.isNotEmpty()) {
                loadImage(category.imagePath)
            }

            itemView.setOnClickListener { onCategoryClick(category) }
        }

        private fun loadImage(imagePath: String) {
            // Check cache first
            imageCache[imagePath]?.let { cachedBitmap ->
                categoryImage.setImageBitmap(cachedBitmap)
                return
            }

            // Load in background
            executor.execute {
                try {
                    val file = File(imagePath)
                    if (file.exists() && imagePath == currentImagePath) {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 2 // Reduce memory usage
                        }
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

                        bitmap?.let {
                            // Cache the bitmap
                            imageCache[imagePath] = it

                            // Only update if this view holder is still showing the same image
                            if (imagePath == currentImagePath) {
                                itemView.post {
                                    categoryImage.setImageBitmap(it)
                                }
                            }
                        } ?: run {
                            Log.w("CategoryAdapter", "Failed to decode bitmap for $imagePath")
                        }
                    } else {
                        Log.w("CategoryAdapter", "File not found or path changed: $imagePath")
                    }
                } catch (e: Exception) {
                    Log.e("CategoryAdapter", "Error loading image: $imagePath", e)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    override fun onViewRecycled(holder: CategoryViewHolder) {
        super.onViewRecycled(holder)
        // Clear current image path when view is recycled
        holder.currentImagePath = null
    }

    fun clearCache() {
        imageCache.clear()
    }
}