package com.utad.mymenu.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.utad.mymenu.databinding.FragmentWeekBinding
import com.utad.mymenu.firebase.real_time_db.RealTimeDBManager
import com.utad.mymenu.firebase.real_time_db.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WeekFragment : Fragment() {

    private lateinit var _binding: FragmentWeekBinding
    private val binding: FragmentWeekBinding get() = _binding

    private val adapter: MealAdapter = MealAdapter { meal -> deleteAction(meal) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabCreateMeal.setOnClickListener {
            navigationToCreateMeal()
        }

        initializeRV()
        getMealsFromFirebase()
    }

    private fun getMealsFromFirebase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = RealTimeDBManager.getMeals()
            withContext(Dispatchers.Main) {
                adapter.submitList(result)
            }
        }
    }

    private fun initializeRV() {
        binding.rvWeek.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvWeek.adapter = adapter
    }

    private fun navigationToCreateMeal() {
        val action = com.utad.mymenu.ui.WeekFragmentDirections.actionWeekFragmentToCreateMealFragment()
        findNavController().navigate(action)
    }

    private fun deleteAction(meal: Meal) {
        RealTimeDBManager.deleteMeal(meal.key!!)
        getMealsFromFirebase()
    }
}