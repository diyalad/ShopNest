package com.example.shopnest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class FilterFragment : Fragment() {

    private var filterListener: FilterListener? = null

    fun setFilterListener(listener: FilterListener) {
        this.filterListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutResId = arguments?.getInt("layoutResId") ?: R.layout.layout_size_filter
        return inflater.inflate(layoutResId, container, false)
    }

    companion object {
        fun newInstance(layoutResId: Int): FilterFragment {
            val fragment = FilterFragment()
            val args = Bundle()
            args.putInt("layoutResId", layoutResId)
            fragment.arguments = args
            return fragment
        }
    }

    interface FilterListener {
        fun onApplyFilter()
        fun onCloseFilter()
    }
}