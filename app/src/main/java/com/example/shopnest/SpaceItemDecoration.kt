package com.example.shopnest

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View


class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
//        outRect.left = space
//        outRect.right = space
//        outRect.bottom = space
//        outRect.top = space
    }
}

