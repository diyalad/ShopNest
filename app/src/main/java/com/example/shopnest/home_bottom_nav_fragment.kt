package com.example.shopnest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class home_bottom_nav_fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_bottom_nav_fragment, container, false)


        // Sample data for categories
//        val categories = listOf(
//            Category("Electronics", R.drawable.electronics_image),
//            Category("Fashion", R.drawable.fashion_image),
//            Category("Home Appliances", R.drawable.home_appliance_image),
//            // Add more categories as needed
//        )

        // Initialize RecyclerView
//        val categoryRecyclerView = view.findViewById<RecyclerView>(R.id.rec_category)
//        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        val categoryAdapter = CategoryAdapter(categories)
//        categoryRecyclerView.adapter = categoryAdapter


    }

}