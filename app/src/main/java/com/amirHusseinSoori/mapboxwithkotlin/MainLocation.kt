package com.amirHusseinSoori.mapboxwithkotlin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.main_fragment.*

class MainLocation:Fragment(R.layout.main_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_RunMapBoxFragment.setOnClickListener {
            findNavController().navigate(R.id.action_mainLocation_to_runMapBoxFragment)
        }
        btn_getSelectedLocation.setOnClickListener {
            val sum = requireActivity().supportFragmentManager
            val fr = GiveLocationFragment()
            fr.show(sum, "")
        }

        btn_search.setOnClickListener {
            findNavController().navigate(R.id.action_mainLocation_to_searchFragment)
        }
    }
}