package com.example.shopnest.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.R
import java.io.File

class ProductImagesAdapter(private val imagePaths: List<String?>) :
    RecyclerView.Adapter<ProductImagesAdapter.ProductImageViewHolder>() {

    interface ImageLoadListener {
        fun onImageLoadSuccess(position: Int, imagePath: String?)
        fun onImageLoadFailure(position: Int, imagePath: String?, error: String)
    }

    private var imageLoadListener: ImageLoadListener? = null

    fun setImageLoadListener(listener: ImageLoadListener) {
        this.imageLoadListener = listener
    }

    inner class ProductImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ProductImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]

        if (imagePath.isNullOrEmpty()) {
            holder.productImage.setImageResource(R.drawable.ic_category_placeholder)
            imageLoadListener?.onImageLoadFailure(
                position,
                imagePath,
                "Empty image path"
            )
            return
        }

        try {
            val file = File(imagePath)
            if (file.exists()) {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                            BitmapFactory.decodeFile(imagePath, this)
                        },
                        1024,
                        1024
                    )
                }

                val bitmap = BitmapFactory.decodeFile(imagePath, options)
                if (bitmap != null) {
                    holder.productImage.setImageBitmap(bitmap)
                    imageLoadListener?.onImageLoadSuccess(position, imagePath)
                } else {
                    throw Exception("Failed to decode bitmap")
                }
            } else {
                holder.productImage.setImageResource(R.drawable.ic_category_placeholder)
                imageLoadListener?.onImageLoadFailure(
                    position,
                    imagePath,
                    "File not found"
                )
            }
        } catch (e: Exception) {
            holder.productImage.setImageResource(R.drawable.ic_category_placeholder)
            imageLoadListener?.onImageLoadFailure(
                position,
                imagePath,
                e.message ?: "Unknown error"
            )
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun getItemCount(): Int = imagePaths.size
}

